package interfazusuario;

//Aca va el main del puesto de atencion para llamar al siguiente

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import negocio.Cliente;
import negocio.ColaIngreso;
import vistas.PanelPuestodeOperacion;

public class funcionesOperador 
{
    private ColaIngreso colaIng = new ColaIngreso();
    private ServerSocket serverSocket;
    private boolean escuchando = false;
    
    public funcionesOperador(){
        
    }
    
    public Cliente getProxCola(){ //puede retornar null --> validar en la otra funcion que se comunica con esta
        Cliente res = colaIng.getProxIngreso();
        return res;
    }
 
    public void iniciarServidor(PanelPuestodeOperacion vistaOperador)
    {
        String puertoStr = vistaOperador.getPuerto().trim();
        
        // Validar que el puerto no esté vacío
        if (puertoStr.isEmpty()) {
            JOptionPane.showMessageDialog(vistaOperador, "Ingrese el puerto.", "Dato faltante", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Validar que sea un número
        int puerto;
        try {
            puerto = Integer.parseInt(puertoStr);
            if (puerto < 1000 || puerto > 65535) {
                JOptionPane.showMessageDialog(vistaOperador, "Ingrese un puerto numérico entre 1000 y 65535.", "Puerto inválido", JOptionPane.ERROR_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(vistaOperador, "El puerto debe ser un número.", "Puerto inválido", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Si ya se está escuchando, no hacer nada
        if (escuchando) {
            JOptionPane.showMessageDialog(vistaOperador, "Ya se está escuchando en el puerto " + puerto, "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        escuchando = true;
        Thread thread = new Thread(() -> ejecutarServidor(puerto, vistaOperador));
        thread.setDaemon(true);
        thread.start();
        
        JOptionPane.showMessageDialog(vistaOperador, "Servidor escuchando en puerto " + puerto, "Conexión", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private void ejecutarServidor(int puerto, PanelPuestodeOperacion vistaOperador) {
        try 
        {
            serverSocket = new ServerSocket(puerto);
            while (escuchando) 
            {
                Socket socket = serverSocket.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String msg = in.readLine();
                
                if (msg != null && !msg.isEmpty()) {
                    try {
                        int dni = Integer.parseInt(msg);
                        colaIng.nuevoIngreso(dni);
                        
                        // Actualizar la interfaz del operador
                        SwingUtilities.invokeLater(() -> vistaOperador.muestraDni());
                        
                        System.out.println("DNI recibido: " + dni);
                    } catch (NumberFormatException e) {
                        System.err.println("DNI inválido: " + msg);
                    }
                }
                socket.close();
            }
        } 
        catch (Exception e)
        {
            if (escuchando) {
                e.printStackTrace();
            }
        }
    }
    
    public void llamarSiguiente(PanelPuestodeOperacion vistaOperador)
    {
        Cliente proxCliente = colaIng.sacarClienteColaIng();
        
        if (proxCliente == null) {
            JOptionPane.showMessageDialog(vistaOperador, "No hay clientes en la cola.", "Cola vacía", JOptionPane.WARNING_MESSAGE);
            vistaOperador.muestraDni();
            return;
        }
        
        String ip = vistaOperador.getIP();
        int puerto = 1111;

        try {
            Socket socket = new Socket(ip, puerto);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(proxCliente.getDNI());
            out.close();
            socket.close();
            
            // Actualizar el operador con el siguiente
            SwingUtilities.invokeLater(() -> vistaOperador.muestraDni());
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(vistaOperador, "No se pudo contactar al monitor:\n" + e.getMessage(), "Error de red", JOptionPane.ERROR_MESSAGE);
        }
    }

}