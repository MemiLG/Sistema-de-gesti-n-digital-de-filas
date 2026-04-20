package negocio;

// aca va la logica del cliente
public class Cliente {
    private int DNI;
    private int puestoAtencion;
    private int estadoLlamada;
    
    public Cliente(int dni){
        this.estadoLlamada = 0;
        this.DNI = dni;
    }
    
    public int getDNI(){
        return this.DNI;
    }
    public void setDNI(int dni){
        this.DNI = dni;
    }
    public int getPuestoAtencion(){
        return this.puestoAtencion;
    }
    public void setPuestoAtencion(int nuevoPuesto){
        this.puestoAtencion = nuevoPuesto;
    }
    public int getEstado(){
        return this.estadoLlamada;
    }
    public void setEstado(int estado){
        this.estadoLlamada = estado;
    }
}
