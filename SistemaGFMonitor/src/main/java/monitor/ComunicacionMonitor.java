package monitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Base64;
import javax.crypto.SecretKey;

public class ComunicacionMonitor extends Thread {
    private Socket socket;
    private Monitor monitor;
    private PrintWriter out;
    private BufferedReader in;
    private boolean ejecutando = true;
    private SecretKey llave;
    
    
    public ComunicacionMonitor(Socket socket, Monitor monitor, SecretKey llave){
        this.socket = socket;
        this.monitor = monitor;
        this.llave = llave;
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
                    enviarPuertoYCifrado(monitor.getPuertoActivo(),monitor.getCifrado());
                }
                case "TERMINAL" ->{
                    monitor.agregarTerminal(this);
                    enviarPuertoYCifrado(monitor.getPuertoActivo(),monitor.getCifrado());
                }
                case "MONITOR" ->{
                    monitor.agregarMonitordeSala(this);
                    enviarPuertoYCifrado(monitor.getPuertoActivo(),monitor.getCifrado());
                }
            }
            while(ejecutando){
                String mensajeCaida = in.readLine();
                if (mensajeCaida != null && mensajeCaida.equals("SERVIDOR_CAIDO")){
                    monitor.setContFallos(3);
                    monitor.servidorCaido();
                }
            }
        } catch(IOException e){}
    }
    
    public void enviarPuertoYCifrado(int puerto, String cifrado){
        String puerto_str = Integer.toString(puerto);
        String llave_str = Base64.getEncoder().encodeToString(this.llave.getEncoded());
        String mensaje = puerto_str+"|"+cifrado+"|"+llave_str;
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
