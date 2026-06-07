
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
import negocio.GestorPS;
import negocio.Historial;
import persistencia.EstadoSistema;

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
    private GestorPS gestorps;

    private HashMap<Integer, Integer> Intentos;     //referencias a los diferentes puestos de atencion concurrentes que se están comunicando 
    private HashMap<Integer,String> puestoEnRenotificacion;          //referencias a los diferentes puestos de registro (terminales) concurrentes que se están comunicando
    
    
    public Servidor(int puerto, int estado){
        this.puerto = puerto;
        colaIng = new ColaIngreso();
        historial = new Historial();
        puestosAtencion = new HashMap<>();
        terminales = new HashMap<>();
        puestoEnRenotificacion = new HashMap<>();
        Intentos = new HashMap<>();
        gestorps = new GestorPS();
        
        this.estado = estado;
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {}

        // Inicializar GestorPS con tipo de almacenamiento y puerto
        try {
            gestorps.cargaEstadoInicial(colaIng, historial, Intentos, puestoEnRenotificacion);
        } catch (Exception e) {
            logger.log(java.util.logging.Level.SEVERE, "Error cargando estado inicial del servidor en puerto " + puerto, e);
        }
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
                   System.out.println("Servidor: nueva apliacion conectada");
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
        gestorps.RCPersistencia(colaIng);
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
        gestorps.GHPersistencia(historial);
    }
    
    public synchronized int verificaHistorial (String cliente){
        return historial.buscaHistorial(cliente);
    }
    
     public synchronized void cambiaHistorial(String cliente, int pos) throws InterruptedException{
         while (pausado) 
             wait();
         historial.eliminaClienteHistorial(pos);
         historial.IngresoHistorial(cliente);
         gestorps.GHPersistencia(historial);
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
    
    public synchronized void inicioRenotificacion(int dni, String numeroInstancia){
        puestoEnRenotificacion.put(dni, numeroInstancia);
        Intentos.put(dni, 1);
        gestorps.guardarEstadoRenotificacion(Intentos, puestoEnRenotificacion);
    }

    public synchronized void modificarEstructurasRenotificacion(int dni, String numeroInstancia) 
    {
        Integer nuevosIntentos = Intentos.get(dni);

        if (nuevosIntentos != null){
                
            nuevosIntentos = nuevosIntentos + 1;
            
            if(nuevosIntentos > 3)
            {
                Intentos.remove(dni);
                puestoEnRenotificacion.remove(dni);
            } else {
                Intentos.put(dni, nuevosIntentos);
                puestoEnRenotificacion.put(dni, numeroInstancia);
            }

            gestorps.guardarEstadoRenotificacion(Intentos, puestoEnRenotificacion);
        }
    }
    
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Uso: java servidor.Servidor <puerto> <rol>");
            System.err.println("Ejemplo: java servidor.Servidor 1234 PRINCIPAL");
            System.exit(1);
        }
        
        try {
            int puerto = Integer.parseInt(args[0]);
            String rol = args[1];
            int estado = rol.equalsIgnoreCase("PRINCIPAL") ? 1 : 2;
            
            Servidor servidor = new Servidor(puerto, estado);
            servidor.start();
            
            System.out.println("Servidor " + rol + " iniciado en puerto " + puerto);
        } catch (NumberFormatException e) {
            System.err.println("Error: El puerto debe ser un número entero");
            System.exit(1);
        } catch (Exception e) {
            System.err.println("Error al iniciar el servidor: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
    
}
