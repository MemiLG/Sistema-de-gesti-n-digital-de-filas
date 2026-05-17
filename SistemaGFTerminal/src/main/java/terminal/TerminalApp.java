
package terminal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import vistas.IngresoTotem;
import static servidor.ConstantesServidor.*;

public class TerminalApp {

    private String IP;
    private static final int puerto = 1234;
    private static final int puertoMonitor = 2345;
    private java.net.Socket socketServidor;
    private java.net.Socket socketMonitor;
    private PrintWriter out;
    private PrintWriter outMonitor;
    private BufferedReader in;
    private BufferedReader inMonitor;
    private String idTerminal = "";
    private IngresoTotem vistaActual;
    private int cantidadFallos = 0;

    
    public TerminalApp()
    {
        
        try {
            IP = InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            IP = "localhost";
        }

    }
    
    private static boolean validacion(IngresoTotem vistaTotem)
    {
        String dni = vistaTotem.getDNI();
        
        if (dni.isBlank()) 
        {
            JOptionPane.showMessageDialog(vistaTotem, "Ingrese su DNI.", "Dato faltante", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        if (dni.length() != 8 )
        {
            JOptionPane.showMessageDialog(vistaTotem, "El DNI debe tener exactamente 8 dígitos.", "Formato incorrecto", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        if (dni.equals("Ej: 12345678"))
        {
            JOptionPane.showMessageDialog(vistaTotem, "Por favor, ingrese su DNI.", "Campo vacío", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (!dni.matches("\\d+")) 
        {
            JOptionPane.showMessageDialog(vistaTotem, "El DNI debe contener solo números.", "Formato incorrecto", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        return true;
    }
    
    public void iniciaConexionMonitor(){

        try{
            socketMonitor = new Socket(IP, puertoMonitor);
            outMonitor = new PrintWriter(socketMonitor.getOutputStream(), true);
            inMonitor = new BufferedReader(new InputStreamReader(socketMonitor.getInputStream()));

            new Thread(() -> {
                try {
                    String mensaje;
                    if ((mensaje = inMonitor.readLine()) != null)
                    {

                        final String msg = mensaje;
                        int puertoServidor = Integer.parseInt(msg);
                        iniciaConexion(puertoServidor);

                    }
                    while ((mensaje = inMonitor.readLine()) != null) 
                    {

                        final String msg = mensaje;
                        int puertoServidor = Integer.parseInt(msg);
                        cerrarConexionMonitor();
                        iniciaConexion(puertoServidor);
                        
                    }
                    cerrarConexionMonitor();

                } catch (IOException e) {
                    e.printStackTrace();

                }
            }).start();
            
        }catch(IOException e){

            JOptionPane.showMessageDialog(null,
                "No se pudo conectar al monitor en " + IP + ":" + puertoMonitor + ".\nVerifique que el monitor esté iniciado.",
                "Error de conexión", JOptionPane.ERROR_MESSAGE);
                
        }

    }

    public void iniciaConexion(int puertoServidor)
    {
        
        try {
            socketServidor = new Socket(IP, puertoServidor);
            out = new PrintWriter(socketServidor.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socketServidor.getInputStream()));

            // identificarse ante el servidor
            out.println("TERMINAL");
            if(out.checkError()){
                this.cantidadFallos++;
                if (!reintentodeEnvio("TERMINAL") )
                {
                    outMonitor.println("SERVIDOR_CAIDO");
                    return;
                } 
            }

            // hilo que escucha respuestas del servidor
            new Thread(() -> {
                try {
                    String mensaje;
                    while ((mensaje = in.readLine()) != null) {
                        final String msg = mensaje;
                        SwingUtilities.invokeLater(() -> procesarMensaje(msg));
                    }
                    cerrarConexionServidor();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                "No se pudo conectar al servidor en " + IP + ":" + puerto + ".\nVerifique que el servidor esté iniciado.",
                "Error de conexión", JOptionPane.ERROR_MESSAGE);
        }

    }

    private void procesarMensaje(String mensaje) {
        if (mensaje.startsWith("ID|")) {
        idTerminal = mensaje.split("\\|")[1]; // guarda el ID asignado
        return;
        }
        switch (mensaje) {
            case CLIENTE_CARGADO -> {
            JOptionPane.showMessageDialog(vistaActual, "Cliente cargado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);

                 if (vistaActual != null) {
                 vistaActual.limpiarDNI();
                }
            }
            case CLIENTE_YA_EXISTE ->
                JOptionPane.showMessageDialog(null, "El cliente ya existe en la fila.", "Cliente existente", JOptionPane.WARNING_MESSAGE);
        }
    }

    public void resetearFallos()
    {
        this.cantidadFallos = 0;
    }

    public void enviarTurno(IngresoTotem vistaTotem)
    {

        if (!validacion(vistaTotem))
            return;
        this.vistaActual = vistaTotem;
        String dni = vistaTotem.getDNI();
        
        out.println(CARGA_NUEVO_CLIENTE);
        if(out.checkError()){
            this.cantidadFallos++;
            if (!reintentodeEnvio(CARGA_NUEVO_CLIENTE) )
            {
               outMonitor.println("SERVIDOR_CAIDO");
                return;
            } 
        }
        out.println(dni);
        if(out.checkError()){
            this.cantidadFallos++;
            if (!reintentodeEnvio(dni) )
            {
                outMonitor.println("SERVIDOR_CAIDO");
                return;
            } 
        }
        resetearFallos();

    }

    private boolean reintentodeEnvio(String mensaje)
    {
        
        while (this.cantidadFallos < 3)
        {
            if (out != null)
            {

                out.println(mensaje);
                if (!out.checkError())
                {
                    return true;
                }
            
                if (this.cantidadFallos < 3)
                {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt(); 
                        return false;
                    }
                }
                this.cantidadFallos++;
                
            }else

                 return false;
            
        }
        return false;
    }

    public void cerrarConexionServidor()
    {

        try 
        {

            if (in!= null)
                in.close();
            
            if (out != null)
                out.close();
            
            if (socketServidor != null && !socketServidor.isClosed())
                socketServidor.close();

        } catch (Exception e) {
            e.printStackTrace();
    
        }

    }

    public void cerrarConexionMonitor()
    {

        try {
          
            socketMonitor.close();
        
        } catch (Exception e) {
    
            e.printStackTrace();
    
        }

    }
    
}

   
