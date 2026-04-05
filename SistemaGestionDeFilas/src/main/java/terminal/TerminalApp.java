
package terminal;

//Este debe ser el main del cliente, donde haga ingreso del DNI

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
        int puerto;
        
        if (dni.isBlank()) 
        {
            
            JOptionPane.showMessageDialog(vistaTotem, "Ingrese su DNI.", "Dato faltante", JOptionPane.WARNING_MESSAGE);
            return;
            
        }
         if (!dni.matches("\\d+")) 
         {
            JOptionPane.showMessageDialog(vistaTotem, "El DNI debe contener solo números.", "Formato incorrecto",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
         
        try {
            puerto = Integer.parseInt(vistaTotem.getPuerto().trim());
            if (puerto < 999 || puerto > 65535) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(vistaTotem, "Ingrese un puerto numérico entre 1000 y 65535.", "Puerto inválido",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }  
        
        
    }
    
    private static void enviarTurno(IngresoTotem vistaTotem)
    {
        
        validacion(vistaTotem);
        
        int dni =  Integer.parseInt(vistaTotem.getDNI());
        String ip = vistaTotem.getIP();
        int puerto = Integer.parseInt(vistaTotem.getPuerto().trim());
            
        new Thread(() -> {
        try {
              String resp = InterfazApp.enviarTurno(host, puerto, dni); // Va al socket o interfazApp ?? 
// Si fue exitosa la conexión muestra cartel de registro completado
              SwingUtilities.invokeLater(() -> mostrarRespuesta(vistaTotem, resp));
        } catch (Exception ex) {
                SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(vistaTotem,
                        "No se pudo contactar al operador:\n" + ex.getMessage(), "Error de red",
                        JOptionPane.ERROR_MESSAGE));
            }
        }, "totem-envio").start();
        
    }
    
    
    
    
}
