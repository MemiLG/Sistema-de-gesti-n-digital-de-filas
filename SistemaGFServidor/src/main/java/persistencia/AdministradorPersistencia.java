package persistencia;

import java.util.HashMap;

import absfactory.JsonFactory;
import absfactory.TextoPlanoFactory;
import absfactory.TipoAlmacenamientoFactory;
import absfactory.XMLFactory;
import archivo.GestorArchivo;
import negocio.ColaIngreso;
import negocio.Historial;

public class AdministradorPersistencia {

    private GestorArchivo gestorArchivo;
    private String tipoAlmacenamiento;
    private int id_servidor;

    public AdministradorPersistencia(String tipo, int id_servidor) 
    {
        this.tipoAlmacenamiento = tipo;
        this.id_servidor = id_servidor;
        TipoAlmacenamientoFactory fabrica = crearFactory();
        if (fabrica != null) {
            this.gestorArchivo = fabrica.crearGestor(id_servidor);
        }
    }

    private TipoAlmacenamientoFactory crearFactory() 
    {
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
            System.getLogger(AdministradorPersistencia.class.getName()).log(System.Logger.Level.WARNING, "GestorArchivo no inicializado, no se puede guardar el estado del sistema.");
            return;
        }
        try
        {
            gestorArchivo.guardarArchivo(estadoSistema);
        
            } catch (Exception e) {
            
            System.getLogger(AdministradorPersistencia.class.getName()).log(System.Logger.Level.ERROR, e);
        
        }
    
    }

    public Integer getIdServidor() {
        return id_servidor;
    }

    public EstadoSistema cargarEstadoSistema()
    {
        if (gestorArchivo == null) {
            System.getLogger(AdministradorPersistencia.class.getName()).log(System.Logger.Level.WARNING, "GestorArchivo no inicializado, no se puede cargar el estado del sistema.");
            return new EstadoSistema();
        }

        try        {   
            EstadoSistema estadoCargado = gestorArchivo.leerArchivo();
            if (estadoCargado != null) {
                return estadoCargado;
            }
        
        } catch (Exception e) {
            System.getLogger(AdministradorPersistencia.class.getName()).log(System.Logger.Level.ERROR, "Error cargando estado del sistema desde puerto " + id_servidor, e);
        
        }
        return new EstadoSistema();

    }

    

    public void guardarColaIngreso(ColaIngreso cola) 
    {
        try
        {
            if (cola == null) {
                System.getLogger(AdministradorPersistencia.class.getName()).log(System.Logger.Level.WARNING, "Cola de ingreso nula");
                return;
            }
            EstadoSistema estado = cargarEstadoSistema();
            if (estado != null && estado.getColaEspera() != null) {
                estado.getColaEspera().clear();
                estado.getColaEspera().addAll(cola.getColaIng());
                guardarEstadoSistema(estado);
            } else {
                System.getLogger(AdministradorPersistencia.class.getName()).log(System.Logger.Level.WARNING, "Estado del sistema o cola de espera nulos");
            }

        } catch (Exception e) {
            System.getLogger(AdministradorPersistencia.class.getName()).log(System.Logger.Level.ERROR, "Error guardando cola de ingreso", e);
        
        }
    }

    public void guardarHistorial(Historial historial) 
    {
        try
        {
            if (historial == null) {
                System.getLogger(AdministradorPersistencia.class.getName()).log(System.Logger.Level.WARNING, "Historial nulo");
                return;
            }
            EstadoSistema estado = cargarEstadoSistema();
            if (estado != null && estado.getHistorialLlamados() != null) {
                estado.getHistorialLlamados().clear();
                estado.getHistorialLlamados().addAll(historial.getHistorial());
                guardarEstadoSistema(estado);
            } else {
                System.getLogger(AdministradorPersistencia.class.getName()).log(System.Logger.Level.WARNING, "Estado del sistema o historial nulos");
            }

        } catch (Exception e) {
            System.getLogger(AdministradorPersistencia.class.getName()).log(System.Logger.Level.ERROR, "Error guardando historial", e);
        }
    }

    public void guardarIntentosRenotificacion(HashMap<Integer,Integer>  intentos, HashMap<Integer,String> puestoEnRenotificacion) 
    {
        try
        {
            if (intentos == null || puestoEnRenotificacion == null) {
                System.getLogger(AdministradorPersistencia.class.getName()).log(System.Logger.Level.WARNING, "Parámetros nulos en guardarIntentosRenotificacion");
                return;
            }
            EstadoSistema estado = cargarEstadoSistema();
            if (estado != null && estado.getIntentosRenotificacion() != null && estado.getPuestoEnRenotificacion() != null) {
                estado.getIntentosRenotificacion().clear();
                estado.getPuestoEnRenotificacion().clear();
                estado.getIntentosRenotificacion().putAll(intentos);
                estado.getPuestoEnRenotificacion().putAll(puestoEnRenotificacion);
                guardarEstadoSistema(estado);
            } else {
                System.getLogger(AdministradorPersistencia.class.getName()).log(System.Logger.Level.WARNING, "Estado del sistema o mapas de renotificación nulos");
            }

        } catch (Exception e) {
            System.getLogger(AdministradorPersistencia.class.getName()).log(System.Logger.Level.ERROR, "Error guardando intentos de renotificación", e);
        }
    }

        
}
