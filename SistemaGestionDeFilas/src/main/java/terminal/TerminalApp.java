
package terminal;

import java.io.BufferedReader;
import java.io.InputStreamReader;

//Este debe ser el main del cliente, donde haga ingreso del DNI

import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import vistas.IngresoTotem;

public class TerminalApp {

    private static String IP;
    private static final int puertoEnvio=1234;
    private static final int puertoRecepcion=1235;
    private static java.net.Socket socket;
    private static PrintWriter out;
    private ServerSocket serverSocket;
    String mensaje;

    
    public TerminalApp()
    {
        
        try {
            IP = InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {

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

    public static void iniciaConexion() 
    {

        try {

            socket = new java.net.Socket(IP, puertoEnvio);
            out = new PrintWriter(socket.getOutputStream(), true);
            out.println("TERMINAL"); // Envía terminal para identificarse en el servidor 
        } catch (Exception e) {
            e.printStackTrace();
        
        }

    }

    public void inicioRecepcion()
    {
        try{

            serverSocket = new ServerSocket(puertoRecepcion);
            Socket clientSocket = serverSocket.accept();
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            mensaje = in.readLine();
        
        } catch (Exception e) {
            e.printStackTrace();

        }
        
    }

    public void enviarTurno(IngresoTotem vistaTotem)
    {

        if (!validacion(vistaTotem))
            return;

        String dni = vistaTotem.getDNI();
    
        out.println("CARGA_NUEVO_CLIENTE|" + dni);
        JOptionPane.showMessageDialog(vistaTotem, "Turno registrado exitosamente.\nDNI: " + dni, "Éxito", JOptionPane.INFORMATION_MESSAGE);
    
    }

    public void cerrarConexion()
    {

        try {
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
    
        }

    }
    public void cerrarRecepcion() 
    {
        try {

            if (serverSocket != null) serverSocket.close();
        } catch (Exception e) {
            e.printStackTrace();

        }
    }
}
    
    
   
