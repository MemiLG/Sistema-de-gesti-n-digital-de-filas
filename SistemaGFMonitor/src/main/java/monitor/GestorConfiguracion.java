package monitor;

import java.io.*;
import java.util.Properties;

public class GestorConfiguracion {

    private static final String ARCHIVO = "configuracion.properties";
    private int puertoPrincipal = 1234;
    private int puertoSecundario = 1235;

    public GestorConfiguracion() {
        File archivo = new File(ARCHIVO);
        if (!archivo.exists()) {
            guardarDefecto(archivo);
        }
        try (FileInputStream fis = new FileInputStream(archivo)) {
            Properties props = new Properties();
            props.load(fis);
            puertoPrincipal = Integer.parseInt(props.getProperty("puerto.principal", "1234"));
            puertoSecundario = Integer.parseInt(props.getProperty("puerto.secundario", "1235"));
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error al leer configuracion.properties, se usan valores por defecto.");
        }
    }

    private void guardarDefecto(File archivo) {
        Properties props = new Properties();
        props.setProperty("puerto.principal", "1234");
        props.setProperty("puerto.secundario", "1235");
        try (FileOutputStream fos = new FileOutputStream(archivo)) {
            props.store(fos, "Configuracion de puertos del sistema de gestion de filas");
        } catch (IOException e) {
            System.err.println("No se pudo crear configuracion.properties.");
        }
    }

    public int getPuertoPrincipal() { return puertoPrincipal; }
    public int getPuertoSecundario() { return puertoSecundario; }
}
