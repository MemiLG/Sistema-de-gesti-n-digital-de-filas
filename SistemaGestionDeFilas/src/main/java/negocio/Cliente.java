package negocio;

// aca va la logica del cliente
public class Cliente {
    private int DNI;
    private int puestoAtencion;
    
    public Cliente(){
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
    
}
