
package negocio;

import java.util.LinkedList;
import java.util.Queue;

public class ColaIngreso {
    private final Queue<Cliente> colaIng;
    
    public ColaIngreso(){
        colaIng = new LinkedList<>();
    }
    
    public Queue<Cliente> getColaIng (){
        return this.colaIng;
    }
    
    public void nuevoIngreso(Cliente cliente){
        colaIng.offer(cliente);
    }
    
    public Cliente sacarClienteColaIng(){ //puede tirar null
        Cliente elem = colaIng.poll();
        return elem;
    }
    
    public Cliente getProxIngreso(){
        return colaIng.peek();
    }
}
