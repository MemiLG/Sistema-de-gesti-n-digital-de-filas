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


public class Monitor {
    private HashMap<Integer,ConexionServidor> servidores;
    private ArrayList<ComunicacionMonitor> puestosdeAtencion;
    private ArrayList<ComunicacionMonitor> terminales;
    private ComunicacionMonitor monitordeSala;
    private String IP;
    private PrintWriter out;
    private BufferedReader in;
    private int contFallos = 0;
    private Ping hiloPing;
    private Echo hiloEcho;
    private int puertoActivo;
    private ServerSocket serverSocket;
    private int puertoMonitor = 2345;
    
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
    
    // --- Conexiones ---
    
    
    public synchronized void agregarServidor(int puerto, int estado){
        try{
            Socket socket = new Socket(IP,puerto);
            ConexionServidor conexion = new ConexionServidor(puerto,socket);
            conexion.setEstado(estado);
            this.servidores.put(puerto,conexion);
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
                   ComunicacionMonitor gestor = new ComunicacionMonitor(clienteSocket,this);
                   new Thread(gestor).start();
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
            int puertoIt = this.puertoActivo;
            Iterator<Integer> it = servidores.keySet().iterator();
            int otroPuerto = -1;
            while (it.hasNext() && otroPuerto == -1) {
                int puerto = it.next();
                if (puerto != this.puertoActivo) {
                    otroPuerto = puerto;
                }
            }
            this.puertoActivo = otroPuerto;
            ConexionServidor con2 = this.servidores.get(this.puertoActivo);
            con2.setEstado(1);
            this.iniciaConexionServidor(this.puertoActivo);
            for(int i=0; i<this.puestosdeAtencion.size(); i+=1){
                this.puestosdeAtencion.get(i).enviarPuerto(this.puertoActivo);
            }
            for (int i = 0; i<this.terminales.size(); i+=1){
                this.terminales.get(i).enviarPuerto(this.puertoActivo);
            }
            this.monitordeSala.enviarPuerto(this.puertoActivo);
            //Hay que salvar al servidor para que se pueda volver pasivo y resincronizarlo
            
        }
    }
    
    public void aumentarFallos(){
        this.contFallos += 1;
    }
    
    /*public void cerrarConexion(){ //Hay que revisar de cerrar la conexion con cada servidor para cerrar bien el sistema

        try {
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }*/
    
    //--- Main de ejecucion ---
    
    public static void main(String[] args){
        Monitor monitor = new Monitor();
        int puerto1 = Integer.parseInt(args[0]);
        int puerto2 = Integer.parseInt(args[1]);
        
        monitor.agregarServidor(puerto1,1);
        monitor.agregarServidor(puerto2,2);
        
        monitor.setPuertoActivo(puerto1);
        monitor.iniciaConexionServidor(puerto1);
        monitor.iniciaConexionApliaciones();
        
        
    }
}
