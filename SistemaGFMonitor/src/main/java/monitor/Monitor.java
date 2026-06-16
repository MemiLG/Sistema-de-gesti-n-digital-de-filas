package monitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JOptionPane;
import negocio.GestorPS;
import servidor.Servidor;
import static servidor.ConstantesServidor.*;
import vistas.IControladorMonitor;
import vistas.vistaApagarMonitor;
import factorySeguridad.LlaveFactory;
import java.util.Base64;
import javax.crypto.SecretKey;


public class Monitor implements IControladorMonitor {
    private HashMap<Integer,ConexionServidor> servidores;
    private ArrayList<ComunicacionMonitor> puestosdeAtencion;
    private ArrayList<ComunicacionMonitor> terminales;
    private ComunicacionMonitor monitordeSala;
    private String IP;
    private PrintWriter out;
    private BufferedReader in;
    private int contFallos = 0;
    private Sincronizacion sincro;
    private Ping hiloPing;
    private Echo hiloEcho;
    private int puertoActivo;
    private ServerSocket serverSocket;
    private int puertoMonitor = 2345;
    private boolean cerrando = false; // true solo cuando el monitor se está apagando intencionalmente
    private Process procesoPrincipal;
    private Process procesoSecundario;
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Servidor.class.getName());
    private SecretKey llave_cifrado;
    private String cifrado;
    private GestorPS gestorPS = new GestorPS();
    
    
    public Monitor(){
        this.servidores = new HashMap<>();
        this.puestosdeAtencion = new ArrayList<>();
        this.terminales = new ArrayList<>();
        try{
            IP = InetAddress.getLocalHost().getHostAddress();
        }
        catch(Exception e){
            IP = "localhost";
        }
    }

    // --- Setters, Getters y Adds ---
    
    public int getPuertoActivo() {
        return puertoActivo;
    }

    public void setPuertoActivo(int puertoActivo) {
        this.puertoActivo = puertoActivo;
    }
    
    public HashMap<Integer,ConexionServidor> getServidores(){
        return this.servidores;
    }

    public ArrayList<ComunicacionMonitor> getPuestosdeAtencion() {
        return puestosdeAtencion;
    }

    public ArrayList<ComunicacionMonitor> getTerminales() {
        return terminales;
    }

    public ComunicacionMonitor getMonitordeSala() {
        return monitordeSala;
    }

    public void setMonitordeSala(ComunicacionMonitor MonitordeSala) {
        this.monitordeSala = MonitordeSala;
    }
    
    
    public synchronized void agregarPuestoAtencion(ComunicacionMonitor puesto){
        this.puestosdeAtencion.add(puesto);
    }
    
    public synchronized void agregarTerminal(ComunicacionMonitor terminal){
        this.terminales.add(terminal);
    }
    
    public synchronized void agregarMonitordeSala(ComunicacionMonitor monitor){
        this.monitordeSala = monitor;
    }
    
       public int getContFallos() {
        return contFallos;
    }

    public void setContFallos(int contFallos) {
        this.contFallos = contFallos;
    }
    
    public boolean estaCerrando(){
        return this.cerrando;
    }
    
    public void iniciaLlave(String cifrado){
        LlaveFactory factory = new LlaveFactory();
        this.llave_cifrado = factory.getLlave(cifrado);
        System.out.println("MONITOR llave: " + Base64.getEncoder().encodeToString(llave_cifrado.getEncoded()));
        System.out.println("MONITOR algoritmo: " + llave_cifrado.getAlgorithm());
    }

    public String getCifrado() {
        return cifrado;
    }

    public void setCifrado(String cifrado) {
        this.cifrado = cifrado;
    }
    
    public String getLlaveString(){
        return Base64.getEncoder().encodeToString(this.llave_cifrado.getEncoded());
    }
    
    public void setTipoPersistencia(String tipo) {
        gestorPS.tipoArchivo(tipo, this.puertoActivo);
    }
    
    // --- Conexiones ---
    
    public synchronized void agregarServidor(int puerto, int estado){
        try{
            Socket socket = new Socket(IP,puerto);
            ConexionServidor conexion = new ConexionServidor(puerto,socket);
            BufferedReader inConexion = new BufferedReader(new InputStreamReader(conexion.getSocket().getInputStream()));
            PrintWriter outConexion = new PrintWriter(conexion.getSocket().getOutputStream(), true);
            conexion.setIn(inConexion);
            conexion.setOut(outConexion);
            conexion.setEstado(estado);
            this.servidores.put(puerto,conexion);
            
            outConexion.println("MONITORSERVIDOR");
            
        } catch(IOException e){
            JOptionPane.showMessageDialog(null,
                "No se pudo conectar al servidor en " + IP + ":" + puerto + ".\nVerifique que el servidor esté iniciado.",
                "Error de conexión", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void iniciaConexionApliaciones(){
        new Thread(()->{
            try{
                this.serverSocket = new ServerSocket(this.puertoMonitor);
                System.out.println("Monitor iniciado");
                while(true){ 
                   Socket clienteSocket = serverSocket.accept();
                   ComunicacionMonitor gestor = new ComunicacionMonitor(clienteSocket,this, this.llave_cifrado);
                   new Thread(gestor).start();
                   System.out.println("Nueva apliacion conectada al monitor");
                }
            }
            catch(Exception e){
                e.printStackTrace();
            }
        
        }).start();
    }
    
    public void iniciaConexionServidor(int puerto){

        try {
            ConexionServidor conexion = this.servidores.get(puerto);
            // Canal de control (ping/echo): se reutiliza el socket "MONITORSERVIDOR" ya abierto en agregarServidor.
            out = conexion.getOut();
            in = conexion.getIn();
            // Timeout de lectura: si el servidor activo deja de responder ECHO, Echo lo detecta.
            conexion.getSocket().setSoTimeout(12000);

            // Canal de sincronización dedicado: separado del canal de echo para no competir por el mismo stream.
            Socket socketSincro = new Socket(IP, puerto);
            PrintWriter outSincro = new PrintWriter(socketSincro.getOutputStream(), true);
            BufferedReader inSincro = new BufferedReader(new InputStreamReader(socketSincro.getInputStream()));
            outSincro.println("MONITORSINCRO");
            conexion.setSocketSincro(socketSincro);

            this.sincro = new Sincronizacion(inSincro, this);
            this.sincro.start();

            this.hiloPing = new Ping(out, this, puerto);
            this.hiloEcho = new Echo(in, this, puerto);
            this.hiloPing.start();
            this.hiloEcho.start();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                "No se pudo conectar al servidor en " + IP + ":" + puerto + ".\nVerifique que el servidor esté iniciado.",
                "Error de conexión", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    public void resetearFallos(){
        this.contFallos = 0;
    }
    
    public synchronized void servidorCaido(int puertoQueFallo){
        // Solo procesar si el que falló es el servidor que está activo ahora mismo.
        // Esto evita que un segundo detector (Ping/Echo) del servidor ya caído dispare
        // un failover sobre el servidor nuevo que recién promovimos.
        if (puertoQueFallo != this.puertoActivo)
            return;
        ConexionServidor conActivo = this.servidores.get(this.puertoActivo);
        if (conActivo == null || conActivo.getEstado() != 1)
            return;

        System.out.println("Monitor: servidor activo (" + this.puertoActivo + ") caido. Iniciando failover.");

        // Marca el servidor caído y detiene los hilos de monitoreo asociados.
        conActivo.setEstado(0);
        if (this.hiloPing != null) this.hiloPing.interrupt();
        if (this.hiloEcho != null) this.hiloEcho.interrupt();
        if (this.sincro != null) this.sincro.interrupt();

        int puertoCaido = this.puertoActivo;

        // Busca un servidor pasivo (estado 2) para promover a activo.
        int otroPuerto = -1;
        for (Integer p : this.servidores.keySet()){
            if (p != puertoCaido && this.servidores.get(p).getEstado() == 2){
                otroPuerto = p;
                break;
            }
        }

        if (otroPuerto == -1){
            System.out.println("Monitor: no hay servidor pasivo disponible para el failover.");
            cerrarConexionCaida(puertoCaido);
            return;
        }

        // Promueve el pasivo a activo.
        this.puertoActivo = otroPuerto;
        this.contFallos = 0;
        ConexionServidor conNuevo = this.servidores.get(this.puertoActivo);
        conNuevo.setEstado(1);
        conNuevo.getOut().println(CAMBIA_ESTADO_ACTIVO); // le avisa al servidor que ahora es el activo

        // Conecta ping/echo/sincronización al nuevo activo.
        this.iniciaConexionServidor(this.puertoActivo);

        // Redirige a todos los clientes hacia el nuevo puerto activo.
        for (ComunicacionMonitor puesto : this.puestosdeAtencion)
            puesto.enviarPuertoYCifrado(this.puertoActivo, this.cifrado);
        for (ComunicacionMonitor terminal : this.terminales)
            terminal.enviarPuertoYCifrado(this.puertoActivo, this.cifrado);
        if (this.monitordeSala != null)
            this.monitordeSala.enviarPuertoYCifrado(this.puertoActivo, this.cifrado);

        // Limpia la conexión del servidor caído.
        cerrarConexionCaida(puertoCaido);

        System.out.println("Monitor: failover completado. Nuevo servidor activo: " + this.puertoActivo);
    }

    private void cerrarConexionCaida(int puerto){
        ConexionServidor con = this.servidores.get(puerto);
        if (con != null){
            try { if (con.getSocket() != null && !con.getSocket().isClosed()) con.getSocket().close(); } catch (IOException e) {}
            try { if (con.getSocketSincro() != null && !con.getSocketSincro().isClosed()) con.getSocketSincro().close(); } catch (IOException e) {}
            this.servidores.remove(puerto);
        }
    }
    
    @Override
    public void cerrarConexion(){

        try {
            this.cerrando = true;
            this.hiloPing.interrupt();
            this.hiloEcho.interrupt();
            this.sincro.interrupt();
            out.println(CERRAR_SERVIDOR);
            for (ConexionServidor conexion : servidores.values()) {
                Socket socket = conexion.getSocket();
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
                Socket socketSincro = conexion.getSocketSincro();
                if (socketSincro != null && !socketSincro.isClosed()) {
                    socketSincro.close();
                }
            }
            for (ComunicacionMonitor puesto : puestosdeAtencion) {
                puesto.detener();
            }
            for (ComunicacionMonitor terminal : terminales) {
                terminal.detener();
            }
            if (this.monitordeSala != null)
                this.monitordeSala.detener();
            this.serverSocket.close();
        } catch (IOException e) {
            logger.log(java.util.logging.Level.SEVERE, "Error al cerrar conexion con servidor", e);
        }
        if (procesoPrincipal != null && procesoPrincipal.isAlive())
            procesoPrincipal.destroy();
        if (procesoSecundario != null && procesoSecundario.isAlive())
            procesoSecundario.destroy();
        System.exit(0);
    }
    
    public synchronized void sincronizar(String funcion){
        
            for (ConexionServidor conexion : servidores.values()) {
                if (conexion.getEstado() == 2){
                    PrintWriter outInterno = conexion.getOut();
                    
                    outInterno.println(funcion);
                }
            }
    }
    
    //--- Main de ejecucion ---
    
    public static void main(){
        Monitor monitor = new Monitor();
        int puerto1 = 1234;
        int puerto2 = 1235;
        
        java.awt.EventQueue.invokeLater(() -> {
            new vistaApagarMonitor(monitor).setVisible(true);
        });
        
        monitor.agregarServidor(puerto1,1);
        monitor.agregarServidor(puerto2,2);
        
        monitor.setPuertoActivo(puerto1);
        monitor.iniciaConexionServidor(puerto1);
        monitor.iniciaConexionApliaciones();
        
        
    }

    @Override
    public void establecerProcesos(Process procesoPrincipal, Process procesoSecundario) {
        this.procesoPrincipal = procesoPrincipal;
        this.procesoSecundario = procesoSecundario;
    }
}
