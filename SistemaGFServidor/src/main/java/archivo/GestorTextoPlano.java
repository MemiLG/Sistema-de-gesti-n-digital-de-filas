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
    private EstadoSistema estadoSistema = new EstadoSistema();


    @Override
    public synchronized EstadoSistema leerArchivo() throws IOException 
    {
        // Implementación para leer el estado del sistema desde un archivo de texto plano
        File archivo = new File(archivotxt);

        if (!archivo.exists()) 
            return new EstadoSistema(); // Retorna un estado vacío si el archivo no existe

        EstadoSistema estadoLeido = new EstadoSistema();
        
        try (BufferedReader r_arch = new BufferedReader(new FileReader(archivo))) {
            String linea;

            while ((linea = r_arch.readLine()) != null) 
            {

                String[] partes = linea.split("\\|");
                if (partes.length < 2) continue; 
                
                String tipo = partes[0];

                switch (tipo) 
                {
                    case "COLA":
                        int dni = Integer.parseInt(partes[1]); 
                        estadoLeido.getColaEspera().add(dni);
                        break;
                    case "HISTORIAL":
                        String llamado = partes[1]; 
                        estadoLeido.getHistorialLlamados().add(llamado);
                        break;
                    case "INTENTOS":
                        String key = partes[1];
                        int intentos = Integer.parseInt(partes[2]);
                        estadoLeido.getIntentosRenotificacion().put(key, intentos);
                        break;
                }

            }
        }

        return estadoLeido;
    }

    @Override
    public synchronized void guardarArchivo(EstadoSistema estado) throws IOException 
    {
        // Implementación para guardar el estado del sistema en un archivo de texto plano
        // Usar try-with-resources para garantizar cierre del archivo
        try (BufferedWriter w_arch = new BufferedWriter(new FileWriter(archivotxt))) {

            for (Integer dni: estado.getColaEspera())
            {
                w_arch.write("COLA|" + dni);
                w_arch.newLine();
            }

            for (String llamado: estado.getHistorialLlamados())
            {
                w_arch.write("HISTORIAL|" + llamado);
                w_arch.newLine();
            }

            for (Map.Entry<String, Integer> entry : estado.getIntentosRenotificacion().entrySet()) 
            {
                w_arch.write("INTENTOS|" + entry.getKey() + "|" + entry.getValue());
                w_arch.newLine();
            }
        }
    }

}
