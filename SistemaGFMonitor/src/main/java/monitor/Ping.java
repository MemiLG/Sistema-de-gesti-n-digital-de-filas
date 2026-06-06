
package monitor;

import java.io.PrintWriter;
import servidor.ConstantesServidor;

public class Ping extends Thread{
    
    private PrintWriter out;
    private boolean canalSano = true;
    private Monitor monitor;
    
    public Ping(PrintWriter out){
        this.out = out;
    }

    @Override
    public void run() {
        try{
            while(canalSano){
                out.println(ConstantesServidor.PING);
                if (out.checkError()) {
                    canalSano = false;
                    monitor.servidorCaido();
                }
                if (canalSano)
                    Thread.sleep(3000);
            }
        } catch(InterruptedException e){
           System.out.println("Ping cierra");
        }
    }
}
