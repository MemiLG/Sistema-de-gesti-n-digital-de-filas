package negocio;

// Aca va la logica
import java.util.Queue;
import java.util.LinkedList;

public class ColaIngreso {
    private final Queue<Integer> colaIng = new LinkedList<>();
    
    public Queue<Integer> getCola (){
        return this.colaIng;
    }
    
    public void nuevoIngreso(int dni){
        colaIng.add(dni);
    }
    
    public int sacarCliente(){
        Integer elem = colaIng.poll();
        if (elem == null){
            elem = 0;
        }
        return elem;
    }
}
