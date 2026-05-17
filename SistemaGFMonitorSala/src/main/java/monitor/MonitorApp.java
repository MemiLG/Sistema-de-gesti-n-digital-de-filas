package monitor;

import java.net.InetAddress;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import negocio.Historial;
import timbre.SonidoApp;
import vistas.PanelMonitordeSala;

//Aca va lo que motraria el monitor de sala
public class MonitorApp {
    private static final int puerto = 1234;
    private static final int puertoMonitor = 2345;
    private String IP = "";
    private Historial historialVentana = new Historial(); //este sera el historial ventana que se mostrará en la ventana
    private Socket socketServidor;
    private Socket socketMonitor;
    private BufferedReader in;
    private BufferedReader inMonitor;
    private PrintWriter out;
    private PrintWriter outMonitor;
    private SonidoApp sonidoRenotificacion = new SonidoApp();
    private int cantidadFallos = 0;


    public MonitorApp(){
        try {
            IP = InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            IP = "localhost";
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

    public void resetearFallos()
    {
        this.cantidadFallos = 0;
    }

    public void iniciaConexionMonitor(PanelMonitordeSala vistaMonitor){

        try{
            socketMonitor = new Socket(IP, puertoMonitor);
            outMonitor = new PrintWriter(socketMonitor.getOutputStream(), true);
            inMonitor = new BufferedReader(new InputStreamReader(socketMonitor.getInputStream()));

            new Thread(() -> {
                try {
                    String mensaje;
                    if ((mensaje = inMonitor.readLine()) != null)
                    {

                        final String msg = mensaje;
                        int puertoServidor = Integer.parseInt(msg);
                        conectar(vistaMonitor, puertoServidor);

                    }
                    while ((mensaje = inMonitor.readLine()) != null) 
                    {

                        final String msg = mensaje;
                        int puertoServidor = Integer.parseInt(msg);
                        cerrarConexionMonitor();
                        conectar(vistaMonitor, puertoServidor);
                        
                    }
                    cerrarConexionMonitor();
                    
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
            
        }catch(IOException e){
            JOptionPane.showMessageDialog(null,
                "No se pudo conectar al monitor en " + IP + ":" + puertoMonitor + ".\nVerifique que el monitor esté iniciado.",
                "Error de conexión", JOptionPane.ERROR_MESSAGE);
        }

    }
    
    

    public void conectar(PanelMonitordeSala vistaMonitor, int puertoServidor) {
        try {
            socketServidor = new Socket(IP, puertoServidor);
            out = new PrintWriter(socketServidor.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socketServidor.getInputStream()));
        
            out.println("MONITOR");
            if(out.checkError()){
                this.cantidadFallos++;
                if (!reintentodeEnvio("MONITOR") )
                {
                    SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null,"No se pudo enviar el turno.","Error de envío", JOptionPane.ERROR_MESSAGE));
                    return;
                } 
            }
            resetearFallos();
        
            Thread thread = new Thread(() -> {
                try {
                    String msg;
                    while ((msg = in.readLine()) != null) {
                        final String mensaje = msg;
                        String[] partes = mensaje.split("\\|");
                        String dni = partes[0];
                        String puesto = partes[1];
                    
                        if (historialVentana.buscaHistorial(dni + " " + puesto) != -1) {
                            historialVentana.eliminaClienteHistorial(historialVentana.buscaHistorial(dni + " " + puesto));
                            sonidoRenotificacion.reproducir("sonido/SonidoTurno.wav");
                            vistaMonitor.iniciarParpadeo();
                        }
                        historialVentana.IngresoHistorial(dni + " " + puesto);
                    
                        SwingUtilities.invokeLater(() -> {
                            vistaMonitor.vizualizarActual();
                            vistaMonitor.visualizarHistorial();
                        });
                    }
                    cerrarConexionServidor();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            thread.setDaemon(true);
            thread.start();
        
        } catch (IOException e) {
            javax.swing.JOptionPane.showMessageDialog(null,
                "No se pudo conectar al servidor en " + IP + ":" + puerto + ".\nVerifique que el servidor esté iniciado.",
                "Error de conexión", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean reintentodeEnvio(String mensaje)
    {
        
        while (this.cantidadFallos < 3)
        {
            if (out != null)
            {

                out.println(mensaje);
                if (!out.checkError())
                {
                    return true;
                }
            
                if (this.cantidadFallos < 3)
                {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt(); 
                        return false;
                    }
                }
                this.cantidadFallos++;
                
            }else{

                JOptionPane.showMessageDialog(null,
                    "No se pudo enviar el mensaje.",
                    "No hay conexión", JOptionPane.ERROR_MESSAGE);
                return false;

            }
        }
        return false;
    }


    public void cerrarConexionServidor()
    {

        try 
        {

            if (in!= null)
                in.close();
            
            if (out != null)
                out.close();
            
            if (socketServidor != null && !socketServidor.isClosed())
                socketServidor.close();

        } catch (Exception e) {
            e.printStackTrace();
    
        }

    }

    public void cerrarConexionMonitor()
    {

        try {
          
            socketMonitor.close();
        
        } catch (Exception e) {
    
            e.printStackTrace();
    
        }

    }

}
