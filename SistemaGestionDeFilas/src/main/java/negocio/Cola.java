package negocio;

//Aca va la logica del historial
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

public class Cola {
    private final Queue<Integer> colaIng = new LinkedList<>();
    private final Stack<Integer> historial = new Stack<>();

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

    public Stack<Integer> getHistorial (){
        return this.historial;
    }

    public int getPosHistorial(int pos){
        return historial.get(pos);
    }

    public int getHistorialSize(){
        return historial.size();
    }

    public void IngresoHistorial(int dni){
        historial.push(dni);
    }
}
