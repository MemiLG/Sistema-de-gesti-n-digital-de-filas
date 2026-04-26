package monitor;

import java.net.InetAddress;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import javax.swing.SwingUtilities;
import negocio.Historial;
import vistas.PanelMonitordeSala;

//Aca va lo que motraria el monitor de sala
public class MonitorApp {
    private static final int puerto = 1111;
    private String IP = "";
    private Historial historial = new Historial();
    
    public MonitorApp(){
        try {
            IP = InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {

        }
    }
    
    
    public String getClientePrimero(){
        if (historial.getHistorialSize() == 0) {
            return null;
        }
        int tope = historial.getHistorialSize() - 1;
        return historial.getPosHistorial(tope);
    }
    
    public Historial getHistorial() {
        return this.historial;
    }

    public void escucha(PanelMonitordeSala vistaMonitor) {
        Thread thread = new Thread(() -> {
            try 
            {
                ServerSocket ssocket = new ServerSocket(puerto);
                while (true) 
                {
                    Socket socket = ssocket.accept();
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    String msg = in.readLine();
                    if (msg != null && !msg.isEmpty()) {
                        try {
                            int dni = Integer.parseInt(msg);
                            historial.IngresoHistorial(String.valueOf(dni));
                            
                            // Actualizar la interfaz desde el hilo principal de Swing
                            SwingUtilities.invokeLater(() -> {
                                vistaMonitor.vizualizarActual();
                                vistaMonitor.visualizarHistorial();
                            });
                            
                            System.out.println("Monitor recibió DNI: " + dni);
                        } catch (NumberFormatException e) {
                            System.err.println("DNI inválido: " + msg);
                        }
                    }
                    socket.close();
                }
            } 
            catch (Exception e)
            {
                e.printStackTrace();
            }
        });
        thread.setDaemon(true);
        thread.start();
    }
}
