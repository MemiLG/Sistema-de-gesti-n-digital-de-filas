
package servidor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
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
    private PrintWriter out; 
    private BufferedReader in;
    
    private HashMap<String,GestorComunicacion> puestosAtencion;     //referencias a los diferentes puestos de atencion concurrentes que se están comunicando 
    private HashMap<String,GestorComunicacion> terminales;          //referencias a los diferentes puestos de registro (terminales) concurrentes que se están comunicando
    private GestorComunicacion monitor;                             //referencia a la comunicacion con el monitor
    
    public Servidor(){
        colaIng = new ColaIngreso();
        historial = new Historial();
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

    public void agregarTerminal(String tipo,GestorComunicacion socket){
        this.terminales.put(tipo, socket);
    }
    
    public void agregarPuestoAtencion(String puesto, GestorComunicacion socket){
        this.puestosAtencion.put(puesto, socket);
    }
    
    public void agregarMonitor(GestorComunicacion socket){
        this.monitor = socket;
    }
    
    public synchronized String verificarCliente(int dni){
        if (colaIng.equals(dni)){
            return CLIENTE_YA_EXISTE;
        } else{
            return CLIENTE_VERIFICADO;
        }
    }
    public synchronized void cargarNuevoCliente(int dni){
        this.colaIng.nuevoIngreso(dni);
    }
    
    public synchronized int siguienteEnCola(){
        return colaIng.getProxIngreso();
    }
}
