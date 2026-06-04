package factory;

import archivo.GestorArchivo;
import archivo.GestorTextoPlano;

public class TextoPlanoFactory implements TipoAlmacenamientoFactory {

    @Override
    public GestorArchivo crearGestor() 
    {
        return new GestorTextoPlano();
    }

}
