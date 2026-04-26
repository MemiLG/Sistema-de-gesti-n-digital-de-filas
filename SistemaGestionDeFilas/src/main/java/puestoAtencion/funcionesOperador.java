package puestoAtencion;

//Aca va el main del puesto de atencion para llamar al siguiente

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import negocio.ColaIngreso;
import vistas.PanelPuestodeOperacion;

public class funcionesOperador 
{
    private ServerSocket serverSocket;
    private String IP;
    private static final int puertoEnvio=1234;
    private static final int puertoRecepcion=1235;
    private static java.net.Socket socket;
    private static PrintWriter out;
    String mensaje;
    int estadoCliente = 0;
    
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
            socket = new java.net.Socket(IP, puertoEnvio);
            out = new PrintWriter(socket.getOutputStream(), true);
            out.println("OPERADOR"); // Envía operador para identificarse en el servidor 
        } catch (Exception e) {
            JOptionPane.showMessageDialog(vistaOperador, "No se pudo conectar al servidor:\n" + e.getMessage(), "Error de conexión", JOptionPane.ERROR_MESSAGE);
        }

    }

    public void llamarSiguiente()
    {
        out.println("LLAMAR_NUEVO_CLIENTE");
        estadoCliente = 1;
        
        // Reiniciar contador de intentos para nuevo cliente
        intentosRenotificacion = 0;
        dniActual = Integer.parseInt(mensaje);
        
        // Actualizar vista
        SwingUtilities.invokeLater(() -> {
            vistaOperador.actualizarContador(intentosRenotificacion);
            vistaOperador.iniciarTimer(30);
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
        
        out.println("RENOVAR_NOTIFICACION");
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
    public void cerrarEnvio() {
        try {
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Inicia la conexion para recibir el mensaje del servidor 
    public void inicioRecepcion()
    {
        try{
            while(true)
            {
                serverSocket = new ServerSocket(puertoRecepcion);
                Socket clientSocket = serverSocket.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                mensaje = in.readLine();
            }
            
        } catch (Exception e) {
            e.printStackTrace();

        }
        
    }

    //Cierra la conexión del socket de recepción al finalizar el puesto de atención
    public void cerrarRecepcion() 
    {
        try {

            if (serverSocket != null) serverSocket.close();
        } catch (Exception e) {
            e.printStackTrace();

        }
    }


}