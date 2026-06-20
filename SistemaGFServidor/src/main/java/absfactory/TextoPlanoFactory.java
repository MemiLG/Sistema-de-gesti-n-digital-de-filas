package absfactory;

import archivo.GestorArchivo;
import archivo.GestorTextoPlano;

public class TextoPlanoFactory implements TipoAlmacenamientoFactory {

    @Override
    public GestorArchivo crearGestor(String nombre)
    {
        return new GestorTextoPlano(nombre);
    }

}
