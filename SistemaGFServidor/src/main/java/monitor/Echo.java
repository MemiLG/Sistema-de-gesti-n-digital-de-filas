package monitor;

import monitor.Monitor;
import java.io.BufferedReader;
import java.io.IOException;
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
        }catch(IOException e){
            monitor.servidorCaido();   
        }
    }
    
    
    
}
