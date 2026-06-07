package absfactory;

import archivo.GestorArchivo;

public interface TipoAlmacenamientoFactory {

    GestorArchivo crearGestor(int puerto);

}
