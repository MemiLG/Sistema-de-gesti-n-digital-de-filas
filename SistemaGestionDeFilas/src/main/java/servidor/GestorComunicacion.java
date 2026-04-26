
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
    private String rol;
    private Servidor servidor;
    private PrintWriter out;
    private BufferedReader in;
    private boolean ejecutando = true;
    
    public GestorComunicacion(Socket socket, Servidor servidor){
        this.socket = socket;
        this.servidor = servidor;
    }
    
    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            String mensaje = in.readLine();
            String[] identificacion = mensaje.split("\\|");
            switch (identificacion[0]){
                case "ATENCION" ->{
                    servidor.agregarPuestoAtencion(identificacion[1], this);
                }
                case "TERMINAL" ->{
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
                            //Tiene que mandarle a la terminal que ya fue cargado
                        } else{
                            if (estado.equals(CLIENTE_VERIFICADO)){
                                servidor.cargarNuevoCliente(dni);
                            }
                        }
                    }
                    case LLAMAR_SIGUIENTE ->{
                        
                        
                    }
                    case RE_NOTIFICAR ->{
                        
                    }
                }
            }
                        
        } catch (IOException ex) {
            System.getLogger(GestorComunicacion.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }
    
    public void setEjecutando(boolean estado){ //Esto lo tengo que transformar en el metodo para detener el gestor (cerrarlo)
        this.ejecutando = estado;
    }
}
