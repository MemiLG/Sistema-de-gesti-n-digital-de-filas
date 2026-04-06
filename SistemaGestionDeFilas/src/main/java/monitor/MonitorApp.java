package monitor;

import vistas.PanelMonitordeSala;

//Aca va lo que motraria el monitor de sala
public class MonitorApp {
    private static final int puerto = 1111;
    

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
