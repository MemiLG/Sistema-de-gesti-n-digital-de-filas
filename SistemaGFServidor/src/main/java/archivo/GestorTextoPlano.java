package archivo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map.Entry;

import persistencia.EstadoSistema;

public class GestorTextoPlano implements GestorArchivo {

    private final String archivotxt;

    public GestorTextoPlano(int id_servidor) {
        this.archivotxt = "estadoSistema_" + id_servidor + ".txt";
    }


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
                        String dni =partes[1]; 
                        estadoLeido.getColaEspera().add(dni);
                        break;
                    case "HISTORIAL":
                        String llamado = partes[1]; 
                        estadoLeido.getHistorialLlamados().add(llamado);
                        break;
                    case "INTENTOS":
                        if (partes.length >= 3) {
                            String dniIntentos = partes[1];
                            String intentos = partes[2];
                            estadoLeido.getIntentosRenotificacion().put(dniIntentos, intentos);
                        }
                        break;
                    case "PUESTO_RENOTIFICACION":
                        if (partes.length >= 3) {             
                            String dniPuesto = partes[1];
                            String puesto = partes[2];
                            estadoLeido.getPuestoEnRenotificacion().put(dniPuesto, puesto);
                        }
                    
                }

            }
        }

        return estadoLeido;
    }

    @Override
    public synchronized void guardarArchivo(EstadoSistema estado) throws IOException 
    {
        if (estado == null) {
            System.getLogger(GestorTextoPlano.class.getName()).log(System.Logger.Level.WARNING, "Intento de guardar EstadoSistema nulo");
            return;
        }
        // Implementación para guardar el estado del sistema en un archivo de texto plano
        // Usar try-with-resources para garantizar cierre del archivo
        try (BufferedWriter w_arch = new BufferedWriter(new FileWriter(archivotxt))) {

            for (String dni: estado.getColaEspera())
            {
                if (dni != null) {
                    w_arch.write("COLA|" + dni);
                    w_arch.newLine();
                }
            }

            for (String llamado: estado.getHistorialLlamados())
            {
                if (llamado != null) {
                    w_arch.write("HISTORIAL|" + llamado);
                    w_arch.newLine();
                }
            }

            if (estado.getIntentosRenotificacion() != null) {
                for (Entry<String, String> entry : estado.getIntentosRenotificacion().entrySet()) 
                {
                    if (entry.getKey() != null && entry.getValue() != null) {
                        w_arch.write("INTENTOS|" + entry.getKey() + "|" + entry.getValue());
                        w_arch.newLine();
                    }
                }
            }

            if (estado.getPuestoEnRenotificacion() != null) {
                for (Entry<String, String> entry : estado.getPuestoEnRenotificacion().entrySet()) 
                {
                    if (entry.getKey() != null && entry.getValue() != null) {
                        w_arch.write("PUESTO_RENOTIFICACION|" + entry.getKey() + "|" + entry.getValue());
                        w_arch.newLine();
                    }
                }
            }
        }
    }

}
