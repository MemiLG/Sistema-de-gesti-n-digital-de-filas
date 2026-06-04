package persistencia;

import absfactory.TextoPlanoFactory;
import absfactory.TipoAlmacenamientoFactory;
import archivo.GestorArchivo;

public class AdministradorPersistencia {

    private GestorArchivo gestorArchivo;

  /*  public AdministradorPersistencia() 
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
            // Agregar más casos para otros tipos de almacenamiento si es necesario
            default:
                throw new IllegalArgumentException("Tipo de almacenamiento no soportado");
        }
    }

    public void guardarEstadoSistema(EstadoSistema estado) 
    {
        try
        {
            gestorArchivo.guardarArchivo(estado);
        
            } catch (Exception e) {
            
            System.getLogger(AdministradorPersistencia.class.getName()).log(System.Logger.Level.ERROR, "Error al guardar el estado del sistema", e);
        
        }
    
    }

    public EstadoSistema cargarEstadoSistema()
    {
        try
        {
            return gestorArchivo.leerArchivo();
        
        } catch (Exception e) {
            
            System.getLogger(AdministradorPersistencia.class.getName()).log(System.Logger.Level.ERROR, "Error al cargar el estado del sistema", e);
        
        }
    }
        */
}
