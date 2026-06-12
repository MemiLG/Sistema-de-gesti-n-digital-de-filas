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
import java.util.Iterator;
import javax.swing.JOptionPane;
import negocio.ColaIngreso;
import negocio.GestorPS;
import negocio.Historial;
import servidor.ConstantesServidor;
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
    private boolean cerrando = true;
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
            out = new PrintWriter(conexion.getSocket().getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(conexion.getSocket().getInputStream()));

            // identificarse ante el servidor
            out.println("MONITORSERVIDOR");
            
            this.sincro = new Sincronizacion(in,this);
            this.sincro.start();
            
            this.hiloPing = new Ping(out);
            this.hiloEcho = new Echo(in, this);
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
    
    public synchronized void servidorCaido(){
        this.hiloPing.interrupt();
        this.hiloEcho.interrupt();
        if (this.contFallos < 3){
            this.contFallos += 1;
            this.hiloPing = new Ping(out);
            this.hiloEcho = new Echo(in, this);
        } else {
            ConexionServidor conect = this.servidores.get(this.puertoActivo);
            conect.setEstado(0);
            out.println(CAMBIA_ESTADO_CAIDO);
            int puertoIt = this.puertoActivo;
            Iterator<Integer> it = servidores.keySet().iterator();
            int otroPuerto = -1;
            while (it.hasNext() && otroPuerto == -1) {
                int puerto = it.next();
                if (puerto != this.puertoActivo) {
                    otroPuerto = puerto;
                }
            }
            this.salvarServidorCaido(this.puertoActivo);
            this.puertoActivo = otroPuerto;
            ConexionServidor con2 = this.servidores.get(this.puertoActivo);
            con2.setEstado(1);
            this.iniciaConexionServidor(this.puertoActivo);
            out.println(CAMBIA_ESTADO_ACTIVO);
            for(int i=0; i<this.puestosdeAtencion.size(); i+=1){
                this.puestosdeAtencion.get(i).enviarPuertoYCifrado(this.puertoActivo,this.cifrado);
            }
            for (int i = 0; i<this.terminales.size(); i+=1){
                this.terminales.get(i).enviarPuertoYCifrado(this.puertoActivo,this.cifrado);
            }
            this.monitordeSala.enviarPuertoYCifrado(this.puertoActivo,this.cifrado);
            
        }
    }
    
    public void aumentarFallos(){
        this.contFallos += 1;
    }
    
    public void salvarServidorCaido(int puertoCaido){
        try{
            Servidor server;
            server = new Servidor(puertoCaido,2,this.cifrado,this.getLlaveString());
            ConexionServidor conexion = this.servidores.get(puertoCaido);
            out.println(ConstantesServidor.ESTADO_INTERNO);
            String snapshot = in.readLine();
            out.println(ConstantesServidor.SNAPSHOT_OK);
            
            String[] partes = snapshot.split(";");
            String[] cola = partes[0].replace("COLA:", "").split(",");
            String[] historial = partes[1].replace("HISTORIAL:", "").split(",");
            ColaIngreso colaCopia = new ColaIngreso();
            for (int i=0; i< cola.length;i+=1){
                colaCopia.nuevoIngreso(cola[i]);
            }
            Historial historialCopia = new Historial();
            for (int i=0;i<historial.length;i+=1){
                historialCopia.IngresoHistorial(historial[i]);
            }
            server.setColaIng(colaCopia);
            server.setHistorial(historialCopia);
            
            String logs = in.readLine(); //Lee los logs que fue recopilando
            String[] operaciones = logs.split("\\|");
            for (String op : operaciones) {
                if (!op.isEmpty()){
                    String[] partesLogs = op.split(":");
                    switch (partesLogs[0]) {
                        case "AGREGAR_COLA" -> server.cargarNuevoCliente(partesLogs[1]);
                        case "LLAMAR_SIGUIENTE" -> server.siguienteEnCola();
                        case "AGREGAR_HISTORIAL" -> server.cargaHistorial(partesLogs[1]);
                        case "CAMBIAR_HISTORIAL" -> {
                            String[] datos = partesLogs[1].split("-");
                            server.cambiaHistorial(datos[0], Integer.parseInt(datos[1]));
                        }
                    }
                }
            }
            conexion.setEstado(2);
            server.start();
        }catch(InterruptedException | IOException e){
            JOptionPane.showMessageDialog(null,"Error al recuperar al servidor caido","Error", JOptionPane.ERROR_MESSAGE);
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
