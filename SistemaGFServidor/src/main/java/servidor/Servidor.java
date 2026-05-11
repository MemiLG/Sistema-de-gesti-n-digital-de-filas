
package servidor;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import negocio.ColaIngreso;
import negocio.Historial;
import static servidor.ConstantesServidor.*;


public class Servidor {
    
    private ColaIngreso colaIng;
    private Historial historial;
    private ServerSocket serverSocket;
    private boolean escuchando = false;
    private String ip = "";
    private int puerto = 1234;
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Servidor.class.getName());
    private AtomicInteger contadorTerminales = new AtomicInteger(0);
    private AtomicInteger contadorPuestos = new AtomicInteger(0);

    
    private HashMap<String,GestorComunicacion> puestosAtencion;     //referencias a los diferentes puestos de atencion concurrentes que se están comunicando 
    private HashMap<String,GestorComunicacion> terminales;          //referencias a los diferentes puestos de registro (terminales) concurrentes que se están comunicando
    private GestorComunicacion monitor;                             //referencia a la comunicacion con el monitor
    
    public Servidor(){
        colaIng = new ColaIngreso();
        historial = new Historial();
        puestosAtencion = new HashMap<>();
        terminales = new HashMap<>();
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {}
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
    
    
    public void iniciar(){
        new Thread(()->{
            try{
                this.serverSocket = new ServerSocket(this.puerto);
                while(true){ 
                   Socket clienteSocket = serverSocket.accept();
                   GestorComunicacion gestor = new GestorComunicacion(clienteSocket,this);
                   new Thread(gestor).start();
                }
            }
            catch(Exception e){
                e.printStackTrace();
            }
        
        }).start();
    }
    
    public void detener(){
        try {
            if (puestosAtencion != null) puestosAtencion.values().forEach(GestorComunicacion::detener);
            if (terminales != null) terminales.values().forEach(GestorComunicacion::detener);
            if (monitor != null) monitor.detener();
            if (serverSocket != null && !serverSocket.isClosed()) serverSocket.close();
        } catch (IOException e) {
            logger.log(java.util.logging.Level.SEVERE, "Error al detener el servidor", e);
        }
    }

    public synchronized String registrarTerminal(GestorComunicacion gestor) {
        String id = String.valueOf(contadorTerminales.incrementAndGet());
        this.terminales.put(id, gestor);
        return id;
    }

    public synchronized String registrarPuestoAtencion(GestorComunicacion gestor) {
        String id = String.valueOf(contadorPuestos.incrementAndGet());
        this.puestosAtencion.put(id, gestor);
        return id;
    }
    
    public synchronized void agregarMonitor(GestorComunicacion socket){
        this.monitor = socket;
    }
    
    public synchronized String verificarCliente(int dni){
        if (colaIng.getColaIng().contains(dni)){
            return CLIENTE_YA_EXISTE;
        } else{
            return CLIENTE_VERIFICADO;
        }
    }
    
    public synchronized void cargarNuevoCliente(int dni){
        this.colaIng.nuevoIngreso(dni);
    }
    
    public synchronized int siguienteEnCola(){
        return colaIng.sacarClienteColaIng();
    }
    
    
    public synchronized void cargaHistorial(String cliente){
        historial.IngresoHistorial(cliente);
    }
    
    public synchronized int verificaHistorial (String cliente){
        return historial.buscaHistorial(cliente);
    }
    
     public synchronized void cambiaHistorial(String cliente, int pos){
         historial.eliminaClienteHistorial(pos);
         historial.IngresoHistorial(cliente);
     }
    
    public synchronized void mandaMonitor(String dni, String puesto){
        if (this.monitor == null) return;
        String mensaje = dni + "|" + puesto;
        this.monitor.enviarMensaje(mensaje);
    }

    public synchronized void mandaTerminal(String mensaje, String numeroTerminal){
        GestorComunicacion g = this.terminales.get(numeroTerminal);
        if (g != null) g.enviarMensaje(mensaje);
    }

    public synchronized void mandaPuestoAtencion(String mensaje, String numeroPuesto){
        GestorComunicacion g = this.puestosAtencion.get(numeroPuesto);
        if (g != null) g.enviarMensaje(mensaje);
    }
}
