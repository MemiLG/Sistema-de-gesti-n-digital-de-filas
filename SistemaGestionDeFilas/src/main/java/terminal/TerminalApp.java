
package terminal;

//Este debe ser el main del cliente, donde haga ingreso del DNI

import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import vistas.IngresoTotem;

public class TerminalApp {

    private static String IP;
    private static final int puerto=1234;
    private static java.net.Socket socket;
    private static PrintWriter out;

    
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

            socket = new java.net.Socket(IP, puerto);
            out = new PrintWriter(socket.getOutputStream(), true);
            out.println("TERMINAL"); // Envía terminal para identificarse en el servidor 
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
    
    /**public static void iniciaConexion(IngresoTotem vistaTotem)
    {   
    
        try {
            Socket socket = new Socket(ip, puerto);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true); // Objeto que permite enviar texto a traves del socket
            out.println("TERMINAL|"+ nroTerminal); // Envía terminal para identificarse en el servidor
            out.close();
            
        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(vistaTotem, "No se pudo contactar al operador:\n" + e.getMessage(), "Error de red", JOptionPane.ERROR_MESSAGE));
        }

    } 
      
    public static void enviarTurno(IngresoTotem vistaTotem)
    {
        if (!validacion(vistaTotem))
            return;
        
        int dni = Integer.parseInt(vistaTotem.getDNI());
    
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true); // Objeto que permite enviar texto a traves del socket
            out.println("CARGAR_CLIENTE|"+ dni); // Envía el DNI
            out.close();
            socket.close();
            
            JOptionPane.showMessageDialog(vistaTotem, "Turno registrado exitosamente.\nDNI: " + dni, "Éxito", JOptionPane.INFORMATION_MESSAGE);
            
        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(vistaTotem, "No se pudo contactar al operador:\n" + e.getMessage(), "Error de red", JOptionPane.ERROR_MESSAGE));
        }
    } **/
}
    
    
   
