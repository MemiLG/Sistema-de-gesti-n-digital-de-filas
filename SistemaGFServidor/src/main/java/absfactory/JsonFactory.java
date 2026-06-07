package absfactory;
import archivo.GestorArchivo;
import archivo.GestorJSON;

public class JsonFactory implements TipoAlmacenamientoFactory {

    @Override
    public GestorArchivo crearGestor(int puerto) 
    {
    
        return new GestorJSON(puerto); 
    }

}
