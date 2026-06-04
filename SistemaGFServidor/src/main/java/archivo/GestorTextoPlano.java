package archivo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import persistencia.EstadoSistema;

public class GestorTextoPlano implements GestorArchivo {

    private static final String archivotxt = "estadoSistema.txt"; //"estador_Sistema" + id_Servidor + ".txt";
    private EstadoSistema estadoSistema;


    @Override
    public EstadoSistema leerArchivo() throws IOException 
    {
        // Implementación para leer el estado del sistema desde un archivo de texto plano
        File archivo = new File(archivotxt);

        if (!archivo.exists()) 
        {
            return new EstadoSistema(); // Retorna un estado vacío si el archivo no existe
        }

        BufferedReader r_arch = new BufferedReader(new FileReader(archivo));
        String linea;

        while ((linea = r_arch.readLine()) != null) 
        {

            String[] partes = linea.split("\\|");
            String tipo = partes[0];

            switch (tipo) 
            {
                case "COLA":
                    int dni = Integer.parseInt(partes[1]); //Ver donde tiene que ir el dni
                    break;
                case "HISTORIAL":
                    String llamado = partes[1]; //Ver donde tiene que ir el el llamado
                    break;
                case "INTENTOS":
                    String key = partes[1];
                    int intentos = Integer.parseInt(partes[2]);
                    break;
            }

        }

        r_arch.close();

        return estadoSistema; // Reemplazar con la lógica real
    }

    @Override
    public void guardarArchivo(EstadoSistema estado) throws IOException 
    {
        // Implementación para guardar el estado del sistema en un archivo de texto plano
         BufferedWriter w_arch = new BufferedWriter(new FileWriter(archivotxt));

        for (Integer dni: estadoSistema.getColaEspera())
        {
            w_arch.write("COLA|" + dni);
            w_arch.newLine();
        }

        for (String llamado: estadoSistema.getHistorialLlamados())
        {
            w_arch.write("HISTORIAL|" + llamado);
            w_arch.newLine();
        }

        for (Map.Entry<String, Integer> entry : estadoSistema.getIntentosRenotificacion().entrySet()) 
        {
            w_arch.write("INTENTOS|" + entry.getKey() + "|" + entry.getValue());
            w_arch.newLine();
        }

        w_arch.close();
    }

}
