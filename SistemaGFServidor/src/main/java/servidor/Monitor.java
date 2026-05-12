
package servidor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;


public class Monitor {
    private ArrayList<Servidor> servidores;
    private String IP;
    private int puerto;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    
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
    
    public void iniciaConexion(PanelPuestodeOperacion vistaOperador)
    {
        
        try {
            socket = new Socket(IP, puerto);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // identificarse ante el servidor
            out.println("MONITOR");

            // hilo que escucha respuestas del servidor
            new Thread(() -> {
                try {
                    String mensaje;
                    while ((mensaje = in.readLine()) != null) {
                        final String msg = mensaje;
                        SwingUtilities.invokeLater(() -> procesarMensaje(msg));
                    }
                    cerrarConexion();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                "No se pudo conectar al servidor en " + IP + ":" + puerto + ".\nVerifique que el servidor esté iniciado.",
                "Error de conexión", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void cerrarConexion(){

        try {
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    
}
