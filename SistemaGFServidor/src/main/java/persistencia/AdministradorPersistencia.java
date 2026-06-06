package persistencia;

import java.util.LinkedList;
import java.util.Map;

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
        if (fabrica != null) {
            this.gestorArchivo = fabrica.crearGestor();
        }
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
            
            default:
                return new TextoPlanoFactory();
        }
    }

    private void guardarEstadoSistema(EstadoSistema estadoSistema) 
    {
        if (gestorArchivo == null) {
            System.getLogger(AdministradorPersistencia.class.getName()).log(System.Logger.Level.WARNING, "GestorArchivo no inicializado");
            return;
        }
        try
        {
            gestorArchivo.guardarArchivo(estadoSistema);
        
            } catch (Exception e) {
            
            System.getLogger(AdministradorPersistencia.class.getName()).log(System.Logger.Level.ERROR, "Error al guardar el estado del sistema", e);
        
        }
    
    }

    public EstadoSistema cargarEstadoSistema()
    {
        if (gestorArchivo == null) {
            System.getLogger(AdministradorPersistencia.class.getName()).log(System.Logger.Level.WARNING, "GestorArchivo no inicializado");
            return new EstadoSistema();
        }
        try
        {   

            EstadoSistema estadoCargado = gestorArchivo.leerArchivo();
            if (estadoCargado != null) {
                this.estadoSistema = estadoCargado;
            }
        
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


    public void guardarIntentosRenotificacion(int intentos, int dni) 
    {
        try
        {
            estadoSistema.getIntentosRenotificacion().put(String.valueOf(dni), intentos);
            guardarEstadoSistema(estadoSistema);

        } catch (Exception e) {
            System.getLogger(AdministradorPersistencia.class.getName()).log(System.Logger.Level.ERROR, "Error al guardar los intentos de renotificación", e);
        }
    }

    public void SacarIntentosRenotificacion(int dni) 
    {
        try
        {
            estadoSistema.getIntentosRenotificacion().remove(String.valueOf(dni));
            guardarEstadoSistema(estadoSistema);

        } catch (Exception e) {
            System.getLogger(AdministradorPersistencia.class.getName()).log(System.Logger.Level.ERROR, "Error al eliminar los intentos de renotificación", e);
        }
    }
        
}
