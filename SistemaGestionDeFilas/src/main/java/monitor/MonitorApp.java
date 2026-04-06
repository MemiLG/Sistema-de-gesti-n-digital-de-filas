package monitor;

import java.util.Stack;
import negocio.Historial;
import vistas.PanelMonitordeSala;

//Aca va lo que motraria el monitor de sala
public class MonitorApp {
    private static final int puerto = 1111;
    private Historial historial = new Historial();
    
    
    public int getDniPrimero(){
        int tope = historial.getHistorialSize() - 1;
        return historial.getPosHistorial(tope);
    }
    
    public Stack<Integer> getPila4(){
        int cantidad = Math.min(5, historial.getHistorialSize());
        int tope = historial.getHistorialSize();
        Stack<Integer> aux = new Stack<>();
        for(int i = tope-cantidad; i <= tope - 2; i++){
            aux.push(historial.getPosHistorial(i));
        }
        return aux;
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
                    vistaMonitor.
                    socket.close();
                }
            } 
            catch (Exception e)
            {
                e.printStackTrace();
            }
        });
        thread.start();


    }
    
    
    
}
