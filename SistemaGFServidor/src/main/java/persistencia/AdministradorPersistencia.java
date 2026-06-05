package persistencia;

import java.util.LinkedList;

import absfactory.JsonFactory;
import absfactory.TextoPlanoFactory;
import absfactory.TipoAlmacenamientoFactory;
import absfactory.XMLFactory;
import archivo.GestorArchivo;
import negocio.ColaIngreso;
import negocio.Historial;

public class AdministradorPersistencia {

    private GestorArchivo gestorArchivo;
    private EstadoSistema estadoSistema = new EstadoSistema();

    public AdministradorPersistencia() 
    {
        TipoAlmacenamientoFactory fabrica = crearFactory();
        this.gestorArchivo = fabrica.crearGestor();
    }

    private TipoAlmacenamientoFactory crearFactory() 
    {
        // Según el tipo de almacenamiento deseado, se puede cambiar la implementación aquí
        switch (tipoAlmacenamiento) {
            case "TEXTO PLANO":
                return new TextoPlanoFactory();
            
            case "JSON":
                return new JsonFactory();
            
            case "XML":
                return new XMLFactory();
        }
    }

    private void guardarEstadoSistema(EstadoSistema estadoSistema) 
    {
        try
        {
            gestorArchivo.guardarArchivo(estadoSistema);
        
            } catch (Exception e) {
            
            System.getLogger(AdministradorPersistencia.class.getName()).log(System.Logger.Level.ERROR, "Error al guardar el estado del sistema", e);
        
        }
    
    }

    public EstadoSistema cargarEstadoSistema()
    {
        try
        {   

            this.estadoSistema = gestorArchivo.leerArchivo(); 
        
        } catch (Exception e) {
            
            System.getLogger(AdministradorPersistencia.class.getName()).log(System.Logger.Level.ERROR, "Error al cargar el estado del sistema", e);
        
        }
        return estadoSistema;
    }

    

    public void guardarColaIngreso(ColaIngreso cola) 
    {
        try
        {
            
            estadoSistema.getColaEspera().clear();
            estadoSistema.getColaEspera().addAll(cola.getColaIng());
            guardarEstadoSistema(estadoSistema);

        } catch (Exception e) {
            
            System.getLogger(AdministradorPersistencia.class.getName()).log(System.Logger.Level.ERROR, "Error al guardar la cola de ingreso", e);
        
        }
    }

    public void guardarHistorial(Historial historial) 
    {
        try
        {
            estadoSistema.getHistorialLlamados().clear();
            estadoSistema.getHistorialLlamados().addAll(historial.getHistorial());
            guardarEstadoSistema(estadoSistema);

        } catch (Exception e) {
            System.getLogger(AdministradorPersistencia.class.getName()).log(System.Logger.Level.ERROR, "Error al guardar el historial", e);
        }
    }

    //Falta guardar intentos de renotificacion
        
}
