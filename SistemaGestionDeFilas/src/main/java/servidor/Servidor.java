
package servidor;
//Aca van a estar las colas. Todos los componentes tienen que comunicarse con el servidor para tener la info que quieren

import java.net.InetAddress;
import java.net.ServerSocket;
import negocio.ColaIngreso;
import negocio.Historial;

public class Servidor {
    private ColaIngreso colaIng;
    private Historial historial;
    private ServerSocket serverSocket;
    private boolean escuchando = false;
    private String ip = "";
    private int puerto;
    
    public Servidor(){
        colaIng = new ColaIngreso();
        historial = new Historial();
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {

        }
    }
    
    public ColaIngreso getColaIng(){
        return this.colaIng;
    }
    
    public Historial getHistorial(){
        return this.historial;
    }
    
    public void setPuerto(int puertoNuevo){
        this.puerto = puertoNuevo;
    }
    
    
}
