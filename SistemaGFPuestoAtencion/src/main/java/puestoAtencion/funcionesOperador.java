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
    private static final int puertoMonitor = 2345;
    private java.net.Socket socketServidor;
    private java.net.Socket socketMonitor;
    private PrintWriter out;
    private PrintWriter outMonitor;
    private BufferedReader in;
    private BufferedReader inMonitor;
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
            IP = "localhost";
        }
        
    }

    public String getDNI()
    {
        return mensaje;
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

    // Inicia la conexión del socket de envio 
    public void iniciaConexion(int puertoServidor)
    {
        
        try {
            socketServidor = new Socket(IP, puertoServidor);
            out = new PrintWriter(socketServidor.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socketServidor.getInputStream()));

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
                    cerrarConexionServidor();
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
            idPuesto = mensaje.split("\\|")[1];
            SwingUtilities.invokeLater(() -> vistaOperador.mostrarIdPuesto(idPuesto));
            return;
        }

        if (mensaje.startsWith(PREFIJO_TAMANO_COLA)) {
            try {
                int cantidad = Integer.parseInt(mensaje.substring(PREFIJO_TAMANO_COLA.length()));
                vistaOperador.actualizarEstadoFila(cantidad);
            } catch (NumberFormatException ignored) {
            }
            return;
        }

        if (mensaje.equals(COLA_VACIA)) {
            JOptionPane.showMessageDialog(vistaOperador,
                "No hay clientes esperando atencion.", "Cola vacía", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        try {
            dniActual = Integer.parseInt(mensaje);
            estadoCliente = 1;
            intentosRenotificacion = 1;
            this.mensaje = String.valueOf(dniActual);
            SwingUtilities.invokeLater(() -> {
                vistaOperador.muestraDni(String.valueOf(dniActual));
                vistaOperador.actualizarContador(intentosRenotificacion);
                vistaOperador.reiniciarBotonRenotificar();
                vistaOperador.iniciarTimer();
            });
        } catch (NumberFormatException e) {
            // mensaje inesperado, ignorar
        }
    }
    


    public void llamarSiguiente()
    {
        out.println("LLAMAR_SIGUIENTE");
    
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
            vistaOperador.iniciarTimer();
        });
    }

    // Cierra la conexión del socket de envio al finalizar el puesto de atención
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