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
import negocio.Cliente;
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

    public funcionesOperador()
    {

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
    }

    public void renotificarCliente()
    {
        out.println("RENOVAR_NOTIFICACION");
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

            serverSocket = new ServerSocket(puertoRecepcion);
            Socket clientSocket = serverSocket.accept();
            BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            mensaje = in.readLine();
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