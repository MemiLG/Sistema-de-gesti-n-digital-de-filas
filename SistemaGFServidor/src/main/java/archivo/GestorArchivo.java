package archivo;

import persistencia.EstadoSistema;

public interface GestorArchivo {

    EstadoSistema leerArchivo() throws Exception;
    void guardarArchivo(EstadoSistema estado) throws Exception;

}
