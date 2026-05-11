
package terminal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import vistas.IngresoTotem;
import static servidor.ConstantesServidor.*;

public class TerminalApp {

    private String IP;
    private static final int puerto = 1234;
    private java.net.Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private String idTerminal = "";

    
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

    public void iniciaConexion() 
    {

        try {
            socket = new Socket(IP, puerto);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // identificarse ante el servidor
            out.println("TERMINAL");

            // hilo que escucha respuestas del servidor
            new Thread(() -> {
                try {
                    String mensaje;
                    while ((mensaje = in.readLine()) != null) {
                        final String msg = mensaje;
                        SwingUtilities.invokeLater(() -> procesarMensaje(msg));
                    }
                    cerrarConexion();
                } catch (IOException e) {
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
            case CLIENTE_CARGADO ->
                JOptionPane.showMessageDialog(null, "Cliente cargado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            case CLIENTE_YA_EXISTE ->
                JOptionPane.showMessageDialog(null, "El cliente ya existe en la fila.", "Cliente existente", JOptionPane.WARNING_MESSAGE);
        }
    }


    public void enviarTurno(IngresoTotem vistaTotem)
    {

        if (!validacion(vistaTotem))
            return;

        String dni = vistaTotem.getDNI();
    
        out.println(CARGA_NUEVO_CLIENTE);
        out.println(dni);
    
    }

    public void cerrarConexion()
    {

        try {
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
    
        }

    }
    
}

   
