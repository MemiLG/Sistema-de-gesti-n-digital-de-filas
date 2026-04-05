
package terminal;

//Este debe ser el main del cliente, donde haga ingreso del DNI

import java.net.Socket;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import vistas.IngresoTotem;

// VAlidar dni, puerto y enviar a Interfaz app
//crea el hilo y envia el dni, puerto e ip para ver si coincide con el puesto de atención

public class TerminalApp {
    
    
    
    public static void main(String[] args) 
    {
        
        SwingUtilities.invokeLater(() -> {
            IngresoTotem vistaTotem = new IngresoTotem();
            vistaTotem.getBotonIngresar().addActionListener(e -> enviarTurno(vistaTotem));
            vistaTotem.setVisible(true);
        });
        
    }
    
    private static void validacion(IngresoTotem vistaTotem)
    {
        String dni = vistaTotem.getDNI();
        int puerto = Integer.parseInt(vistaTotem.getPuerto().trim());
        
        if (dni.isBlank()) 
        {
            
            JOptionPane.showMessageDialog(vistaTotem, "Ingrese su DNI.", "Dato faltante", JOptionPane.WARNING_MESSAGE);
            return;
            
        }
        if (!dni.matches("\\d+")) 
        {
            JOptionPane.showMessageDialog(vistaTotem, "El DNI debe contener solo números.", "Formato incorrecto", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (puerto < 999 || puerto > 65535)

            JOptionPane.showMessageDialog(vistaTotem, "Ingrese un puerto numérico entre 1000 y 65535.", "Puerto inválido", JOptionPane.ERROR_MESSAGE); 
        
        
    }
    
    private static void enviarTurno(IngresoTotem vistaTotem)
    {
        
        validacion(vistaTotem);
        
        int dni =  Integer.parseInt(vistaTotem.getDNI());
        String ip = vistaTotem.getIP();
        int puerto = Integer.parseInt(vistaTotem.getPuerto().trim());
    
        try {
            //
            Socket conexion = new Socket(ip,puerto);
            PrintWriter out = new PrintWriter(conexion.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            
        } catch (Exception e) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(vistaTotem,
                        "No se pudo contactar al operador:\n" + ex.getMessage(), "Error de red",
                        JOptionPane.ERROR_MESSAGE));
            }
        }, "totem-envio").start();
        
    }
    
    
    
    
}
