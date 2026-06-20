package absfactory;

import archivo.GestorArchivo;

public interface TipoAlmacenamientoFactory {

    GestorArchivo crearGestor(String nombre);

}
