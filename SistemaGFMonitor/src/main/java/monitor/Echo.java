package monitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.SocketTimeoutException;
import servidor.ConstantesServidor;

public class Echo extends Thread {
    private BufferedReader in;
    private Monitor monitor;
    private int puerto;

    public Echo(BufferedReader in, Monitor monitor, int puerto){
        this.in = in;
        this.monitor = monitor;
        this.puerto = puerto;
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
            // readLine() devolvió null: el servidor cerró la conexión (EOF) => probablemente caído
            if (!Thread.currentThread().isInterrupted() && !monitor.estaCerrando()){
                System.out.println("Echo cierra por servidor caido (fin de stream)");
                monitor.servidorCaido(puerto);
            }
            else
                System.out.println("Echo cierra por llamado del monitor(para cerrar normalmente)");
        }catch(SocketTimeoutException e){
            if(!Thread.currentThread().isInterrupted() && !monitor.estaCerrando()){
                System.out.println("Echo cierra por servidor caido (timeout)");
                monitor.servidorCaido(puerto);
            }
            else
                System.out.println("Echo cierra por llamado del monitor(para cerrar normalmente)");
        }catch(IOException e){
            if (!Thread.currentThread().isInterrupted() && !monitor.estaCerrando()){
                System.out.println("Echo cierra por servidor caido");
                monitor.servidorCaido(puerto);
            }
            else
                System.out.println("Echo cierra por llamado del monitor(para cerrar normalmente)");
        }
    }
    
    
    
}
