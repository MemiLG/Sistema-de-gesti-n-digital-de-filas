package monitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ComunicacionMonitor extends Thread {
    private Socket socket;
    private Monitor monitor;
    private PrintWriter out;
    private BufferedReader in;
    private boolean ejecutando = true;
    
    
    public ComunicacionMonitor(Socket socket, Monitor monitor){
        this.socket = socket;
        this.monitor = monitor;
        try {
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new PrintWriter(socket.getOutputStream(), true);
        }
        catch(IOException e){}
    }
    
    @Override
    public void run() {
        try{
            String mensaje = in.readLine();
            switch (mensaje){
                case "ATENCION" ->{
                    monitor.agregarPuestoAtencion(this);
                    enviarPuerto(monitor.getPuertoActivo());
                }
                case "TERMINAL" ->{
                    monitor.agregarTerminal(this);
                    enviarPuerto(monitor.getPuertoActivo());
                }
                case "MONITOR" ->{
                    monitor.agregarMonitordeSala(this);
                    enviarPuerto(monitor.getPuertoActivo());
                }
            }
            while(ejecutando){
                String mensajeCaida = in.readLine();
                if (mensaje != null && mensajeCaida.equals("SERVIDOR_CAIDO")){
                    monitor.setContFallos(3);
                    monitor.servidorCaido();
                }
            }
        } catch(IOException e){}
    }
    
    public void enviarPuerto(int puerto){
        String mensaje = Integer.toString(puerto);
        out.println(mensaje);
    }
    
    public void detener(){
        ejecutando = false;
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            System.getLogger(ComunicacionMonitor.class.getName()).log(System.Logger.Level.ERROR, "Error al cerrar el gestor", e);
        }
    }
}
