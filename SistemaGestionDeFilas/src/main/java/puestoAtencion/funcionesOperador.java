package puestoAtencion;

//Aca va el main del puesto de atencion para llamar al siguiente

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import negocio.ColaIngreso;
import vistas.PanelPuestodeOperacion;
import static servidor.ConstantesServidor.*;

public class funcionesOperador 
{
    private String IP;
    private static final int puerto=1234;
    private java.net.Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    String mensaje;
    int estadoCliente = 0;
    private String idPuesto = "";
    
    // Control de reintentos y timer
    private int intentosRenotificacion = 0;
    private int dniActual = 0;
    private Thread timerThread;
    private boolean timerActivo = false;
    private PanelPuestodeOperacion vistaOperador;

    public funcionesOperador(PanelPuestodeOperacion vista)
    {
        this.vistaOperador = vista;
        try {
            IP = InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {

        }
        
    }

    public String getDNI()
    {
        return mensaje;
    }

    // Inicia la conexión del socket de envio 
    public void iniciaConexion(PanelPuestodeOperacion vistaOperador)
    {
        
        try {
            socket = new Socket(IP, puerto);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // identificarse ante el servidor
            out.println("ATENCION");

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
            e.printStackTrace();
        }
    }
    
    private void procesarMensaje(String mensaje) {
        if (mensaje.startsWith("ID|")) {
            idPuesto = mensaje.split("\\|")[1];
            SwingUtilities.invokeLater(() -> vistaOperador.mostrarIdPuesto(idPuesto));
            return;
        }

        if (mensaje.equals(COLA_VACIA)) {
            JOptionPane.showMessageDialog(vistaOperador,
                "La cola está vacía.", "Cola vacía", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            dniActual = Integer.parseInt(mensaje);
            this.mensaje = String.valueOf(dniActual);
            SwingUtilities.invokeLater(() -> {
                vistaOperador.muestraDni(String.valueOf(dniActual));
                vistaOperador.iniciarTimer(30);
                vistaOperador.activarBotonRenotar();
            });
        } catch (NumberFormatException e) {
            // mensaje inesperado, ignorar
        }
    }
    


    public void llamarSiguiente()
    {
        out.println("LLAMAR_SIGUIENTE");
        estadoCliente = 1;
        intentosRenotificacion = 0;
      
        SwingUtilities.invokeLater(() -> {
            vistaOperador.actualizarContador(intentosRenotificacion);
        });
    }

    public void renotificarCliente()
    {
        // Verificar que no haya excedido intentos
        if (intentosRenotificacion >= 3) {
            JOptionPane.showMessageDialog(vistaOperador, 
                "Ya se alcanzó el máximo de 3 llamadas para este cliente.",
                "Límite alcanzado", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        out.println(RENOVAR_NOTIFICACION);
        out.println(dniActual);
        estadoCliente += 1;
        
        // Incrementar intento de renotificación
        intentosRenotificacion++;
        
        // Actualizar vista
        SwingUtilities.invokeLater(() -> {
            vistaOperador.actualizarContador(intentosRenotificacion);
            
            // Si llegó a 3 intentos, desactivar botón volver a llamar
            if (intentosRenotificacion >= 3) {
                vistaOperador.desactivarBotonRenotar();
            }
            
            // Bloquear ambos botones por 30 segundos
            vistaOperador.iniciarTimer(30);
        });
    }

    // Cierra la conexión del socket de envio al finalizar el puesto de atención
    public void cerrarConexion()
    {

        try {
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
    
        }

    }

}