package absfactory;
import archivo.GestorArchivo;
import archivo.GestorJSON;

public class JsonFactory implements TipoAlmacenamientoFactory {

    @Override
    public GestorArchivo crearGestor(String nombre)
    {
        return new GestorJSON(nombre);
    }

}
