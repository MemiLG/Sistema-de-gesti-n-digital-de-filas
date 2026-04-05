
package terminal;

//Este debe ser el main del cliente, donde haga ingreso del DNI

import java.io.PrintWriter;
import java.net.Socket;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import vistas.IngresoTotem;

// VAlidar dni, puerto y enviar a Interfaz app
//crea el hilo y envia el dni, puerto e ip para ver si coincide con el puesto de atención

public class TerminalApp {
    
    private static boolean validacion(IngresoTotem vistaTotem)
    {
        String dni = vistaTotem.getDNI();
        String puertoStr = vistaTotem.getPuerto().trim();
        
        if (dni.isBlank()) 
        {
            JOptionPane.showMessageDialog(vistaTotem, "Ingrese su DNI.", "Dato faltante", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (!dni.matches("\\d+")) 
        {
            JOptionPane.showMessageDialog(vistaTotem, "El DNI debe contener solo números.", "Formato incorrecto", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        if (vistaTotem.getIP().isBlank())
        {
            JOptionPane.showMessageDialog(vistaTotem, "Ingrese la IP del servidor.", "Dato faltante", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        if (puertoStr.isBlank())
        {
            JOptionPane.showMessageDialog(vistaTotem, "Ingrese el puerto.", "Dato faltante", JOptionPane.WARNING_MESSAGE);
            return false;
        }
        
        try {
            int puerto = Integer.parseInt(puertoStr);
            if (puerto < 1000 || puerto > 65535)
            {
                JOptionPane.showMessageDialog(vistaTotem, "Ingrese un puerto numérico entre 1000 y 65535.", "Puerto inválido", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(vistaTotem, "El puerto debe ser un número.", "Puerto inválido", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        return true;
    }
    
    public static void enviarTurno(IngresoTotem vistaTotem)
    {
        if (!validacion(vistaTotem))
            return;
        
        int dni = Integer.parseInt(vistaTotem.getDNI());
        String ip = vistaTotem.getIP();
        int puerto = Integer.parseInt(vistaTotem.getPuerto().trim());
    
        try {
            Socket socket = new Socket(ip, puerto);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(dni);
            out.close();
            socket.close();
            
            JOptionPane.showMessageDialog(vistaTotem, "Turno registrado exitosamente.\nDNI: " + dni, "Éxito", JOptionPane.INFORMATION_MESSAGE);
            
        } catch (Exception e) {
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(vistaTotem, "No se pudo contactar al operador:\n" + e.getMessage(), "Error de red", JOptionPane.ERROR_MESSAGE));
        }
    } 
        
}
    
    
   
