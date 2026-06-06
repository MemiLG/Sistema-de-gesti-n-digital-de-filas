package archivo;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import persistencia.EstadoSistema;

public class GestorJSON implements GestorArchivo {

    private static final String archivojson = "estadoSistema.json"; //"estador_Sistema" + id_Servidor + ".json";
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Override
    public EstadoSistema leerArchivo() throws IOException 
    {
        File archjson = new File(archivojson);

        if (!archjson.exists()) 
            return new EstadoSistema(); 

        try (BufferedReader br = new BufferedReader(new FileReader(archjson))) {
            EstadoSistema estadoSis = gson.fromJson(br, EstadoSistema.class);
            if (estadoSis == null) {
                System.getLogger(GestorJSON.class.getName()).log(System.Logger.Level.WARNING, "JSON vacío o inválido. Retornando EstadoSistema vacío.");
                return new EstadoSistema();
            }
            return estadoSis;
        }
    }


    @Override
    public void guardarArchivo(EstadoSistema estado) throws IOException 
    {
        if (estado == null) {
            System.getLogger(GestorJSON.class.getName()).log(System.Logger.Level.WARNING, "Intento de guardar EstadoSistema nulo");
            return;
        }
        String json = gson.toJson(estado);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(archivojson))) {
            bw.write(json);
        }
    }

}
