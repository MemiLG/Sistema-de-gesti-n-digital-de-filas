
package monitor;

import java.io.PrintWriter;
import servidor.ConstantesServidor;

public class Ping extends Thread{
    
    private PrintWriter out;
    
    public Ping(PrintWriter out){
        this.out = out;
    }

    @Override
    public void run() {
        try{
            while(true){
                out.println(ConstantesServidor.PING);
                Thread.sleep(3000);
            }
        } catch(InterruptedException e){
            //ver como cerrar la transmicion
        }
    }
}
