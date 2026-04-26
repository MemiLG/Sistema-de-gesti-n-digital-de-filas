
package servidor;

//Este es el hilo que se abrirá cuando una aplicaion quiera comunicarse con el servidor

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class GestorComunicacion implements Runnable {
    
    private Socket socket;
    private String rol;
    private Servidor servidor;
    private PrintWriter out;
    private BufferedReader in;
    
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
            if ("ATENCION".equals(identificacion[0])){
                //mandarle al servidor la info
            }
                        
        } catch (IOException ex) {
            System.getLogger(GestorComunicacion.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }
    
}
