package negocio;

//Aca va la logica del historial
import java.util.ArrayList;
import java.util.Iterator;

public class Historial implements Iterable<String>{
    private final ArrayList<String> historial;
    
    public Historial(){
        historial = new ArrayList<>();
    }

    public ArrayList<String> getHistorial (){
        return this.historial;
    }

    public String getPosHistorial(int pos){
        return historial.get(pos);
    }

    public int getHistorialSize(){
        return historial.size();
    }

    public void IngresoHistorial(String cliente){
        historial.add(0, cliente);
    }
    
    public int buscaHistorial(String cliente){
        return historial.indexOf(cliente);
    }
    
    public void eliminaClienteHistorial(int pos){
        historial.remove(pos);
    }
    
    public void pasaHistorial(String cliente){
        historial.add(cliente);
    }
    
    @Override
    public Iterator iterator() {
        return historial.iterator();
    }
}
