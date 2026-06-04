package absfactory;

import archivo.GestorArchivo;
import archivo.GestorXML;

public class XMLFactory implements TipoAlmacenamientoFactory {

    @Override
    public GestorArchivo crearGestor() 
    {
        return new GestorXML(); 
    }

}
