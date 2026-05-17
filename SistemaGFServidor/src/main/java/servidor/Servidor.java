
package servidor;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import negocio.ColaIngreso;
import negocio.Historial;
import static servidor.ConstantesServidor.*;


public class Servidor extends Thread{
    
    private ColaIngreso colaIng;
    private Historial historial;
    private ServerSocket serverSocket;
    private int estado;
    private boolean escuchando = false;
    private String ip = "";
    private int puerto;
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Servidor.class.getName());
    private AtomicInteger contadorTerminales = new AtomicInteger(0);
    private AtomicInteger contadorPuestos = new AtomicInteger(0);

    
    private HashMap<String,GestorComunicacion> puestosAtencion;     //referencias a los diferentes puestos de atencion concurrentes que se están comunicando 
    private HashMap<String,GestorComunicacion> terminales;          //referencias a los diferentes puestos de registro (terminales) concurrentes que se están comunicando
    private GestorComunicacion monitor;                             //referencia a la comunicacion con el monitor
    private GestorComunicacion monitorServidor;
    private GestorComunicacion monitorSincronizacion;
    
    private boolean pausado = false;
    private boolean logActivo = false;
    private ArrayList<String> logOperaciones = new ArrayList<>();
    
    public Servidor(int puerto, int estado){
        this.puerto = puerto;
        colaIng = new ColaIngreso();
        historial = new Historial();
        puestosAtencion = new HashMap<>();
        terminales = new HashMap<>();
        this.estado = estado;
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {}
    }
    
    //--- Getters y Setters ---
    
    public ColaIngreso getColaIng(){
        return this.colaIng;
    }
    
    public Historial getHistorial(){
        return this.historial;
    }
    
    public void setPuerto(int puertoNuevo){
        this.puerto = puertoNuevo;
    }

    public void setColaIng(ColaIngreso colaIng) {
        this.colaIng = colaIng;
    }

    public void setHistorial(Historial historial) {
        this.historial = historial;
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }
    
    //--- Conexion ---
    public void iniciar(){
        new Thread(()->{
            try{
                this.serverSocket = new ServerSocket(this.puerto);
                System.out.println("Servidor con puerto "+ this.puerto +" iniciado");
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
            if (monitorServidor != null) monitorServidor.detener();
            if (serverSocket != null && !serverSocket.isClosed()) serverSocket.close();
            System.out.println("Servidor con puerto "+this.puerto+" apagado");
        } catch (IOException e) {
            logger.log(java.util.logging.Level.SEVERE, "Error al detener el servidor", e);
        } finally {
            Thread.currentThread().interrupt();
        }
    }

    //--- Funciones de recursos compartidos ---
    
    public synchronized String obtenerSnapshot() {
    StringBuilder sbCola = new StringBuilder("COLA:");
    Queue<Integer> colaActual = colaIng.getColaIng();
    sbCola.append(colaActual.stream().map(String::valueOf).collect(Collectors.joining(",")));
    
    StringBuilder sbHistorial = new StringBuilder(";HISTORIAL:");
    ArrayList<String> histActual = historial.getHistorial();
    sbHistorial.append(histActual.stream().collect(Collectors.joining(",")));
    
    logActivo = true;
    logOperaciones.clear();
    
    return sbCola.toString() + sbHistorial.toString();
    }

    public synchronized String obtenerLogYPausar() {
        pausado = true;
        String logStr = String.join("|", logOperaciones);
        logOperaciones.clear();
        logActivo = false;
        return logStr;
    }

    public synchronized void reanudar() {
        pausado = false;
        notifyAll();
    }

    private synchronized void loggear(String operacion) {
        if (logActivo) {
            logOperaciones.add(operacion);
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
    
    public void agregarMonitorServidor(GestorComunicacion socket){
        this.monitorServidor = socket;
    }
    
    public void agregaMonitorSincronizacion(GestorComunicacion socket){
        this.monitorSincronizacion = socket;
    }
    
    public synchronized String verificarCliente(int dni){
        if (colaIng.getColaIng().contains(dni)){
            return CLIENTE_YA_EXISTE;
        } else{
            return CLIENTE_VERIFICADO;
        }
    }
    
    public synchronized void cargarNuevoCliente(int dni) throws InterruptedException{
        while(pausado)
            wait();
        this.colaIng.nuevoIngreso(dni);
        loggear("AGREGAR_COLA:" + dni);
    }
    
    public synchronized int siguienteEnCola() throws InterruptedException{
        while (pausado) 
            wait();
        int dni = colaIng.sacarClienteColaIng();
        loggear("LLAMAR_SIGUIENTE:" + dni);
        return dni;
    }
    
    
    public synchronized void cargaHistorial(String cliente) throws InterruptedException{
        while (pausado) 
            wait();
        historial.IngresoHistorial(cliente);
        loggear("AGREGAR_HISTORIAL:" + cliente);
    }
    
    public synchronized int verificaHistorial (String cliente){
        return historial.buscaHistorial(cliente);
    }
    
     public synchronized void cambiaHistorial(String cliente, int pos) throws InterruptedException{
         while (pausado) 
             wait();
         historial.eliminaClienteHistorial(pos);
         historial.IngresoHistorial(cliente);
         loggear("CAMBIAR_HISTORIAL:" + cliente + " " + pos);
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

    public synchronized int tamanoColaEspera() {
        return colaIng.getColaIng().size();
    }

    public synchronized void notificarTamanoColaATodosLosPuestos() {
        String mensaje = PREFIJO_TAMANO_COLA + tamanoColaEspera();
        for (GestorComunicacion gestor : puestosAtencion.values()) {
            gestor.enviarMensaje(mensaje);
        }
    }
    
    public synchronized void mandaMonitorServidor(){
        if (this.monitorServidor != null)
            this.monitorServidor.enviarMensaje(ECHO);
    }
    
    public synchronized void mandaFuncionesMonitor(String funcion){
        if (this.monitorSincronizacion != null)
            this.monitorSincronizacion.enviarMensaje(funcion);
    }
    
    @Override
    public void run() {
        this.iniciar();
    }
    
    
}
