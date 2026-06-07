package persistencia;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlElement;

@XmlRootElement(name = "estadoSistema")
public class EstadoSistema {

    @XmlElement
    private ArrayList<String> historialLlamados;
    
    @XmlElement
    private List<Integer> colaEspera;
    
    @XmlElement
    private HashMap<Integer, Integer> intentos;

    @XmlElement
    private HashMap<Integer,String> puestoEnRenotificacion; 

    public EstadoSistema() 
    {
        this.colaEspera = new ArrayList<>();
        this.historialLlamados = new ArrayList<>();
        this.intentos = new HashMap<>();
        this.puestoEnRenotificacion = new HashMap<>();
    }

    public List<Integer> getColaEspera()
    {
        return colaEspera;
    }

    public List<String> getHistorialLlamados() 
    {
        return historialLlamados;
    }

    public Map<Integer, Integer> getIntentosRenotificacion() 
    {
        return intentos;
    }

    public Map<Integer, String> getPuestoEnRenotificacion() 
    {
        return puestoEnRenotificacion;
    }

}
