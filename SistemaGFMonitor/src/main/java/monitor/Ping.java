
package monitor;

import java.io.PrintWriter;
import servidor.ConstantesServidor;

public class Ping extends Thread{

    private PrintWriter out;
    private boolean canalSano = true;
    private Monitor monitor;
    private int puerto;

    public Ping(PrintWriter out, Monitor monitor, int puerto){
        this.out = out;
        this.monitor = monitor;
        this.puerto = puerto;
    }

    @Override
    public void run() {
        try{
            while(canalSano && !isInterrupted()){
                out.println(ConstantesServidor.PING);
                if (out.checkError()) {
                    canalSano = false;
                    if (!isInterrupted() && !monitor.estaCerrando())
                        monitor.servidorCaido(puerto);
                }
                if (canalSano)
                    Thread.sleep(3000);
            }
        } catch(InterruptedException e){
           System.out.println("Ping cierra");
        }
    }
}
