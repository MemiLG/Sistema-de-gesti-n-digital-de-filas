
package servidor;
//Aca van a estar las colas. Todos los componentes tienen que comunicarse con el servidor para tener la info que quieren

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import negocio.ColaIngreso;
import negocio.Historial;

public class Servidor {
    private ColaIngreso colaIng;
    private Historial historial;
    private ServerSocket serverSocket;
    private boolean escuchando = false;
    private String ip = "";
    private int puerto;
    private ArrayList<GestorComunicacion> puestosAtencion; //punteros a los diferentes puestos de atencion concurrentes que se están comunicando 
    private ArrayList<GestorComunicacion> terminales; //punteros a los diferentes puestos de registro (terminales) concurrentes que se están comunicando
    private GestorComunicacion monitor; //puntero a la comunicacion con el monitor
    
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
    
    /*    public void iniciarServidor(PanelPuestodeOperacion vistaOperador)
    {
        String puertoStr = vistaOperador.getPuerto().trim();
        
        // Validar que el puerto no esté vacío
        if (puertoStr.isEmpty()) {
            JOptionPane.showMessageDialog(vistaOperador, "Ingrese el puerto.", "Dato faltante", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Validar que sea un número
        int puerto;
        try {
            puerto = Integer.parseInt(puertoStr);
            if (puerto < 1000 || puerto > 65535) {
                JOptionPane.showMessageDialog(vistaOperador, "Ingrese un puerto numérico entre 1000 y 65535.", "Puerto inválido", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(vistaOperador, "El puerto debe ser un número.", "Puerto inválido", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Si ya se está escuchando, no hacer nada
        if (escuchando) {
            JOptionPane.showMessageDialog(vistaOperador, "Ya se está escuchando en el puerto " + puerto, "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        escuchando = true;
        Thread thread = new Thread(() -> ejecutarServidor(puerto, vistaOperador));
        thread.setDaemon(true);
        thread.start();
        
        JOptionPane.showMessageDialog(vistaOperador, "Servidor escuchando en puerto " + puerto, "Conexión", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void ejecutarServidor(int puerto, PanelPuestodeOperacion vistaOperador) {
        try 
        {
            serverSocket = new ServerSocket(puerto);
            while (escuchando) 
            {
                Socket socket = serverSocket.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String msg = in.readLine();
                
                if (msg != null && !msg.isEmpty()) {
                    try {
                        int dni = Integer.parseInt(msg);
                        colaIng.nuevoIngreso(dni);
                        
                        // Actualizar la interfaz del operador
                        SwingUtilities.invokeLater(() -> vistaOperador.muestraDni());
                        
                        System.out.println("DNI recibido: " + dni);
                    } catch (NumberFormatException e) {
                        System.err.println("DNI inválido: " + msg);
                    }
                }
                socket.close();
            }
        } 
        catch (Exception e)
        {
            if (escuchando) {
                e.printStackTrace();
            }
        }
    }*/
}
