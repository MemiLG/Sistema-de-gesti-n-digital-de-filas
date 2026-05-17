package monitor;

import java.io.BufferedReader;
import java.io.IOException;


public class Sincronizacion extends Thread{

    private BufferedReader in;
    private Monitor monitor;
    
    public Sincronizacion(BufferedReader in, Monitor monitor){
        this.in = in;
        this.monitor = monitor;
    }

    public BufferedReader getIn() {
        return in;
    }

    public void setIn(BufferedReader in) {
        this.in = in;
    }

    public Monitor getMonitor() {
        return monitor;
    }

    public void setMonitor(Monitor monitor) {
        this.monitor = monitor;
    }
    
    
    
    @Override
    public void run() {
        try{
            String mensaje;
            while((mensaje = in.readLine()) != null){
                monitor.sincronizar(mensaje);
            }
        }catch(IOException e){
            if (!Thread.currentThread().isInterrupted()){
                System.out.println("Sincronizacion cierra");
            }else {
                System.getLogger(Sincronizacion.class.getName()).log(System.Logger.Level.ERROR, (String) null, e);
            }
        } 
    }
    
}
