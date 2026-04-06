package negocio;

//Aca va la logica del historial
import java.util.Stack;

public class Historial {
    private final Stack<Integer> historial;
    
    public Historial(){
        historial = new Stack<>();
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
