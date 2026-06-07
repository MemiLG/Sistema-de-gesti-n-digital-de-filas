package absfactory;

import archivo.GestorArchivo;
import archivo.GestorTextoPlano;

public class TextoPlanoFactory implements TipoAlmacenamientoFactory {

    @Override
    public GestorArchivo crearGestor(int puerto) 
    {
        return new GestorTextoPlano(puerto);
    }

}
