
package negocio;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

public class ColaIngreso implements Iterable<String>{
    private final Queue<String> colaIng;
    
    public ColaIngreso(){
        colaIng = new LinkedList<>();
    }
    
    public Queue<String> getColaIng (){
        return this.colaIng;
    }
    
    public void nuevoIngreso(String dni){
        colaIng.offer(dni);
    }
    
    public String sacarClienteColaIng(){
        String elem = colaIng.poll();
        if (elem == null){
            elem = null;
        }
        return elem;
    }
    
    public String getProxIngreso(){
        return colaIng.peek();
    }
    
    public void addCliente(String dni){
        colaIng.add(dni);
    }

    @Override
    public Iterator iterator() {
        return colaIng.iterator();
    }
}
