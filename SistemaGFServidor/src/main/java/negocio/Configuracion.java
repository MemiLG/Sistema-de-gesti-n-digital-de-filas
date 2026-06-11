package negocio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class Configuracion {

    private static final String ARCHIVO = "config.properties";

    public static String getFormatoAlmacenamiento(){
    
        return getPropiedad("formato.almacenamiento", "TEXTO PLANO");
    }

    public static void setFormatoAlmacenamiento(String tipo) 
    {
        setPropiedad("formato.almacenamiento", tipo);
    }

    public static int getPuertoServidor()
    {
        return Integer.parseInt(getPropiedad("puertoservidor","1235"));
    }

    private static String getPropiedad(String clave, String valorDefault) {
        Properties props = new Properties();
        try (FileInputStream fis = new FileInputStream(ARCHIVO)) {
            props.load(fis);
            return props.getProperty(clave, valorDefault);
        } catch (IOException e) {
            return valorDefault;
        }
    }

    private static void setPropiedad(String clave, String valor) {
        Properties props = new Properties();
        File archivo = new File(ARCHIVO);

        if (archivo.exists()) {
            try (FileInputStream fis = new FileInputStream(archivo)) {
                props.load(fis);
            } catch (IOException e) {
                System.err.println("Error leyendo config: " + e.getMessage());
            }
        }

        props.setProperty(clave, valor);

        try (FileOutputStream fos = new FileOutputStream(archivo)) {
            props.store(fos, "Configuracion actualizada automaticamente");
        } catch (IOException e) {
            System.err.println("Error guardando config: " + e.getMessage());
        }
    }
    
}
