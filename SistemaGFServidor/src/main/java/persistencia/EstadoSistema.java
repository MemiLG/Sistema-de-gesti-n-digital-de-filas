package persistencia;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class EstadoSistema {

    private ArrayList<String> historialLlamados;
    private List<Integer> colaEspera;
    private Map<String, Integer> intentosRenotificacion;

    public EstadoSistema() 
    {

        this.colaEspera = new LinkedList<>();
        this.historialLlamados = new ArrayList<>();
        this.intentosRenotificacion = new HashMap<>();

    }

    public List<Integer> getColaEspera()
    {
        return colaEspera;
    }

    public List<String> getHistorialLlamados() 
    {
        return historialLlamados;
    }

    public Map<String, Integer> getIntentosRenotificacion() 
    {
        return intentosRenotificacion;
    }

}
