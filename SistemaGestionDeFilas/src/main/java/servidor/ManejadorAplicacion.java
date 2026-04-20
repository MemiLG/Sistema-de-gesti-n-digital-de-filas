
package servidor;

//Este es el hilo que se abrirá cuando una aplicaion quiera comunicarse con el servidor

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ManejadorAplicacion implements Runnable {
    
    private Socket socket;
    private Thread t;
    private String rol;
    
    public ManejadorAplicacion(Socket socket){
        this.socket = socket;
        this.t = new Thread();
        t.start();
    }
    
    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            
        } catch (IOException ex) {
            System.getLogger(ManejadorAplicacion.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }
    
}
