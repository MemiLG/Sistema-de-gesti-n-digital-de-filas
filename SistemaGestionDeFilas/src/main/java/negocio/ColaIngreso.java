
package negocio;

import java.util.LinkedList;
import java.util.Queue;

public class ColaIngreso {
    private final Queue<Integer> colaIng;
    
    public ColaIngreso(){
        colaIng = new LinkedList<>();
    }
    
    public Queue<Integer> getColaIng (){
        return this.colaIng;
    }
    
    public void nuevoIngreso(int dni){
        colaIng.add(dni);
    }
    
    public int sacarClienteColaIng(){
        Integer elem = colaIng.poll();
        if (elem == null){
            elem = 0;
        }
        return elem;
    }
    
    public int getProxIngreso(){
        return colaIng.peek();
    }
}
