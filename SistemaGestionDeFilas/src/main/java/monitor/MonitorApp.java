package monitor;

import java.net.InetAddress;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import javax.swing.SwingUtilities;
import negocio.Historial;
import vistas.PanelMonitordeSala;

//Aca va lo que motraria el monitor de sala
public class MonitorApp {
    private static final int puerto = 1234;
    private String IP = "";
    private Historial historialVentana = new Historial(); //este sera el historial ventana que se mostrará en la ventana
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out; 
    
    public MonitorApp(){
        try {
            IP = InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {

        }
    }
    
    public String getClientePrimero(){
        if (historialVentana.getHistorialSize()==0){
            return null;
        }
        else{
            return historialVentana.getPosHistorial(0);
        }
    }
    public Historial getHistorial() {
        return this.historialVentana;
    }
    
    

    public void conectar(PanelMonitordeSala vistaMonitor) {
        try {
            socket = new Socket(IP, puerto);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        
            out.println("MONITOR");
        
            Thread thread = new Thread(() -> {
                try {
                    String msg;
                    while ((msg = in.readLine()) != null) {
                        final String mensaje = msg;
                        String[] partes = mensaje.split("\\|");
                        String dni = partes[0];
                        String puesto = partes[1];
                    
                        historialVentana.IngresoHistorial(dni + " " + puesto);
                    
                        SwingUtilities.invokeLater(() -> {
                            vistaMonitor.vizualizarActual();
                            vistaMonitor.visualizarHistorial();
                        });
                    }
                    cerrarConexion();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            thread.setDaemon(true);
            thread.start();
        
        } catch (IOException e) {
            e.printStackTrace();
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
