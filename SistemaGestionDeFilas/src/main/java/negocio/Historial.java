package negocio;

//Aca va la logica del historial
import java.util.Stack;

public class Historial {
    private final Stack<Cliente> historial;
    
    public Historial(){
        historial = new Stack<>();
    }

    public Stack<Cliente> getHistorial (){
        return this.historial;
    }

    public Cliente getPosHistorial(int pos){
        return historial.get(pos);
    }

    public int getHistorialSize(){
        return historial.size();
    }

    public void IngresoHistorial(Cliente nuevoCliente){
        historial.push(nuevoCliente);
    }
}
