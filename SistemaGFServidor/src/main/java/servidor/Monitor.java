
package servidor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import javax.swing.JOptionPane;


public class Monitor {
    private ArrayList<Servidor> servidores;
    private String IP;
    private int puerto;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private int contFallos = 0;
    
    public Monitor(){
        this.servidores = new ArrayList<>();
        this.puerto = 1234; 
        try{
            IP = InetAddress.getLocalHost().getHostAddress();
        }
        catch(Exception e){
            IP = "localhost";
        }
    }
    
    public ArrayList<Servidor> getServidores(){
        return this.servidores;
    }
    
    public void iniciaConexion()
    {
        
        try {
            socket = new Socket(IP, puerto);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // identificarse ante el servidor
            out.println("MONITOR");
            
            new Ping(out).start();
            new Echo(in, this).start();
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                "No se pudo conectar al servidor en " + IP + ":" + puerto + ".\nVerifique que el servidor esté iniciado.",
                "Error de conexión", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void resetearFallos(){
        this.contFallos = 0;
    }
    
    public void servidorCaido(){
        if (this.contFallos < 3)
            this.contFallos += 1;
            // El hilo tiene que seguir escuchando -> Ver si por la excepcion se cae y hay que volver a 
        //Avisa de que se cayo el servidor a las demás aplicaciones
    }
    
    public void aumentarFallos(){
        this.contFallos += 1;
    }
    
    public void cerrarConexion(){

        try {
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    
}
