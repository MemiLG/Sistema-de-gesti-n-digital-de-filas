package negocio;

//Aca va la logica del historial

import java.util.LinkedList;
import java.util.Queue;

public class ColaHistorial {
    
    private final Queue<Integer> colaHistorial = new LinkedList<>();
    
    public Queue<Integer> getHIstorial (){
        return this.colaHistorial;
    }
    
    public void IngresoHistorial(int dni){
        colaHistorial.add(dni);
    }
    
    
}
