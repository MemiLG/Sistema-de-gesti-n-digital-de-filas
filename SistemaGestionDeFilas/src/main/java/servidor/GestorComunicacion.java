
package servidor;

//Este es el hilo que se abrirá cuando una aplicaion quiera comunicarse con el servidor

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import static servidor.ConstantesServidor.*;

public class GestorComunicacion implements Runnable {
    
    private Socket socket;
    private Servidor servidor;
    private String numeroInstancia;
    private PrintWriter out;
    private BufferedReader in;
    private boolean ejecutando = true;
    
    public GestorComunicacion(Socket socket, Servidor servidor){
        this.socket = socket;
        this.servidor = servidor;
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        }
        catch(IOException e){
        
        }
    }
    
    @Override
    public void run() {
        try {
            
            String mensaje = in.readLine();
            String[] identificacion = mensaje.split("\\|");
            switch (identificacion[0]){
                case "ATENCION" ->{
                    this.numeroInstancia = identificacion[1];
                    servidor.agregarPuestoAtencion(identificacion[1], this);
                }
                case "TERMINAL" ->{
                    this.numeroInstancia = identificacion[1];
                    servidor.agregarTerminal(identificacion[1], this);
                }
                case "MONITOR" ->{
                    servidor.agregarMonitor(this);
                }
            }
            while (ejecutando){
                String funcion = in.readLine();
                switch (funcion){
                    case CARGA_NUEVO_CLIENTE ->{
                        String num = in.readLine();
                        int dni = Integer.parseInt(num);
                        String estado = servidor.verificarCliente(dni);
                        if (estado.equals(CLIENTE_YA_EXISTE)){
                            servidor.mandaTerminal(CLIENTE_YA_EXISTE, this.numeroInstancia);
                        } else{
                            if (estado.equals(CLIENTE_VERIFICADO)){
                                servidor.cargarNuevoCliente(dni);
                            }
                        }
                    }
                    case LLAMAR_SIGUIENTE ->{
                        int siguiente_dni = servidor.siguienteEnCola();
                        out.println(siguiente_dni); //Porque está hablando con el puesto de atencion.
                        String clienteHistorial = Integer.toString(siguiente_dni)+" "+this.numeroInstancia;
                        servidor.cargaHistorial(clienteHistorial);
                        servidor.mandaMonitor(Integer.toString(siguiente_dni), this.numeroInstancia);
                        
                    }
                    case RENOVAR_NOTIFICACION ->{
                        String dni_renotif = in.readLine(); //el puesto de atencion le manda el dni de la persona que quiere vover a llamar
                        String dni_renotif_entero = dni_renotif + " " + this.numeroInstancia;
                        int estado = servidor.verificaHistorial(dni_renotif_entero);
                        if (estado != -1){
                            servidor.cambiaHistorial(dni_renotif_entero, estado);
                            servidor.mandaMonitor(dni_renotif, this.numeroInstancia);
                        }
                    }
                }
            }
                        
        } catch (IOException ex) {
            System.getLogger(GestorComunicacion.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }
    
    public void enviarMensaje(String mensaje){
        out.println(mensaje);
    }
    
    public void setEjecutando(boolean estado){ //Esto lo tengo que transformar en el metodo para detener el gestor (cerrarlo)
        this.ejecutando = estado;
    }
    
   
}
