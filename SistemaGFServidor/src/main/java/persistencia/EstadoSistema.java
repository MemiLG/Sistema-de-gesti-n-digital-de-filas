package persistencia;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlTransient;

import negocio.ColaIngreso;

@XmlRootElement(name = "estadoSistema")
public class EstadoSistema {

    @XmlElement
    private ArrayList<String> historialLlamados;
    
    @XmlElement
    private List<Integer> colaEspera;
    
    @XmlTransient
    private Map<String, Integer> intentosRenotificacion;

    public EstadoSistema() 
    {
        this.colaEspera = new ArrayList<>();
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
