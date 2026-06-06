package monitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.SocketTimeoutException;
import servidor.ConstantesServidor;

public class Echo extends Thread {
    private BufferedReader in;
    private Monitor monitor;
    
    public Echo(BufferedReader in, Monitor monitor){
        this.in = in;
        this.monitor = monitor;
    }

    @Override
    public void run() {
        try{
            String mensaje;
            while((mensaje = in.readLine()) != null){
                if (mensaje.equals(ConstantesServidor.ECHO)){
                    monitor.resetearFallos();
                }
            }
        }catch(SocketTimeoutException e){
            if(!Thread.currentThread().isInterrupted() && monitor.estaCerrando()){
                System.out.println("Echo cierra por servidor caido");
                monitor.servidorCaido();
            }
            else 
                System.out.println("Echo cierra por llamado del monitor(para cerrar normalmente)");
        }catch(IOException e){
            if (!Thread.currentThread().isInterrupted()&& monitor.estaCerrando()){
                System.out.println("Echo cierra por servidor caido");
                monitor.servidorCaido();
            }
            else 
                System.out.println("Echo cierra por llamado del monitor(para cerrar normalmente)");
        }
    }
    
    
    
}
