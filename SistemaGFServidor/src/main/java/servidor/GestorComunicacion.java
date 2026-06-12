
package servidor;

//Este es el hilo que se abrirá cuando una aplicaion quiera comunicarse con el servidor

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import javax.swing.JOptionPane;
import static servidor.ConstantesServidor.*;

public class GestorComunicacion implements Runnable {
    
    private Socket socket;
    private Servidor servidor;
    private String numeroInstancia = "";
    private PrintWriter out;
    private BufferedReader in;
    private boolean ejecutando = true;
    private int rol;
    
    public GestorComunicacion(Socket socket, Servidor servidor){
        this.socket = socket;
        this.servidor = servidor;
        try {
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new PrintWriter(socket.getOutputStream(), true);
        }
        catch(IOException e){
        
        }
    }
    
    @Override
    public void run() {
        try {
            
            String mensaje = in.readLine();
            switch (mensaje){
                case "ATENCION" ->{
                    String id = servidor.registrarPuestoAtencion(this);
                    this.numeroInstancia = id;
                    enviarMensaje("ID|" + id);
                    servidor.notificarTamanoColaATodosLosPuestos();
                    servidor.enviarEstadoRenotificacion(this, id);
                    this.rol=0;
                }
                case "TERMINAL" ->{
                    String id = servidor.registrarTerminal(this);
                    this.numeroInstancia = id;
                    enviarMensaje("ID|" + id);
                    this.rol=0;
                }
                case "MONITOR" ->{
                    servidor.agregarMonitor(this);
                    this.rol=0;
                }
                case "MONITORSERVIDOR" ->{
                    servidor.agregarMonitorServidor(this);
                    this.rol=1;
                }
                case "MONITORSINCRO" ->{
                    servidor.agregarMonitorServidor(this);
                    this.rol = 0;
                }
            }
            while (ejecutando){
                String funcion = in.readLine();
                if (funcion != null){ 
                    switch (funcion){
                        case CARGA_NUEVO_CLIENTE ->{
                            try{
                                if (servidor.getEstado() == 1)
                                    servidor.mandaFuncionesMonitor(funcion);
                                String num = in.readLine();
                                String estado = servidor.verificarCliente(num);
                                if (estado.equals(CLIENTE_YA_EXISTE) && servidor.getEstado() == 1){
                                    servidor.mandaTerminal(CLIENTE_YA_EXISTE, this.numeroInstancia);
                                } else{
                                    if (estado.equals(CLIENTE_VERIFICADO)){
                                        servidor.cargarNuevoCliente(num);
                                        if (servidor.getEstado() == 1){
                                            servidor.mandaTerminal(CLIENTE_CARGADO, this.numeroInstancia);
                                            servidor.notificarTamanoColaATodosLosPuestos();
                                        }
                                    }
                                }
                            }catch (InterruptedException e){
                                JOptionPane.showMessageDialog(null,"No se pudo cargar el nuevo cliente","Error", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                        case LLAMAR_SIGUIENTE ->{
                            try{
                                if (servidor.getEstado() == 1)
                                    servidor.mandaFuncionesMonitor(funcion);
                                String siguiente_dni = servidor.siguienteEnCola();
                                if (siguiente_dni != null){
                                    out.println(siguiente_dni); 
                                    String clienteHistorial = siguiente_dni+" "+this.numeroInstancia;
                                    servidor.cargaHistorial(clienteHistorial);
                                    servidor.inicioRenotificacion(siguiente_dni, this.numeroInstancia);
                                    if (servidor.getEstado() == 1)
                                        servidor.mandaMonitor(siguiente_dni, this.numeroInstancia);
                                }
                                else {
                                    if (servidor.getEstado() == 1)
                                        servidor.mandaPuestoAtencion(COLA_VACIA, this.numeroInstancia);
                                }
                                if (servidor.getEstado() == 1)
                                    servidor.notificarTamanoColaATodosLosPuestos();
                            }catch (InterruptedException e){
                                JOptionPane.showMessageDialog(null,"No se pudo llamar al siguiente","Error", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                        case RENOVAR_NOTIFICACION ->{
                            try{
                                if (servidor.getEstado() == 1)
                                    servidor.mandaFuncionesMonitor(funcion);
                                String dni_renotif = in.readLine();
                                System.out.println("entra en renotificacion" + dni_renotif);
                                String dni_renotif_enc = servidor.getEncriptador().encriptar(dni_renotif);
                                String dni_renotif_entero = dni_renotif_enc + " " + this.numeroInstancia;
                                int estado = servidor.verificaHistorial(dni_renotif_entero);
                                if (estado != -1){
                                    servidor.cambiaHistorial(dni_renotif_entero, estado);
                                    servidor.modificarEstructurasRenotificacion(dni_renotif_enc, this.numeroInstancia);
                                    if (servidor.getEstado() == 1)
                                        servidor.mandaMonitor(dni_renotif_enc, this.numeroInstancia);
                                }
                            }catch (InterruptedException e){
                                JOptionPane.showMessageDialog(null,"No se pudo renovar la notificacion","Error", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                        case PING ->{
                            servidor.mandaMonitorServidor();
                        }
                        case ESTADO_INTERNO ->{
                            String snapshot = servidor.obtenerSnapshot();
                            this.enviarMensaje(snapshot);
                        }
                        case SNAPSHOT_OK ->{
                            String logResidual = servidor.obtenerLogYPausar();
                            out.println(logResidual);
                            servidor.reanudar();
                        }
                        case CAMBIA_ESTADO_CAIDO ->{
                            servidor.setEstado(0);
                        }
                        case CAMBIA_ESTADO_PASIVO ->{
                            servidor.setEstado(2);
                        }
                        case CAMBIA_ESTADO_ACTIVO ->{
                            servidor.setEstado(1);
                        }
                        case CERRAR_SERVIDOR ->{
                            System.out.println("Llego el mensaje de cerrar el servidor");
                            servidor.detener();
                        }
                    }
                } else{
                    ejecutando = false;
                    servidor.detener();
                }
            }             
        } catch (IOException ex) { //depende si esta excepcion aparece en el gestor del monitor, ahi tiene que mandar al servidor a automatarse
            if (ejecutando){
                if (rol == 0)
                    System.getLogger(GestorComunicacion.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
                else
                    servidor.detener();
            }
        }
    }
    
    public void enviarMensaje(String mensaje){
        out.println(mensaje);
    }
    
    public void setEjecutando(boolean estado){ //Esto lo tengo que transformar en el metodo para detener el gestor (cerrarlo)
        this.ejecutando = estado;
    }
    
    public void detener() {
        ejecutando = false;
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            System.getLogger(GestorComunicacion.class.getName()).log(System.Logger.Level.ERROR, "Error al cerrar el gestor", e);
        }
    }
   
}