package interfazusuario;

//Aca va el main del puesto de atencion para llamar al siguiente

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import negocio.Cola;
import vistas.PanelPuestodeOperacion;

public class funcionesOperador 
{
    private Cola colaIngreso = new Cola();
 
    public void iniciarServidor(PanelPuestodeOperacion vistaOperador)
    {
        Thread thread = new Thread(() -> {
            try 
            {
                ServerSocket ssocket = new ServerSocket(Integer.parseInt(vistaOperador.getPuerto()));
                while (true) 
                {
                    Socket socket = ssocket.accept();
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    String msg = in.readLine();
                    colaIngreso.nuevoIngreso(Integer.parseInt(msg));
                    socket.close();
                }
            } 
            catch (Exception e)
            {
                e.printStackTrace();
            }
        });
        thread.start();
    }
    
    public void llamarSiguiente(PanelPuestodeOperacion vistaOperador)
    {

        int dni = Integer.parseInt(vistaOperador.getDNI());
        String ip = vistaOperador.getIP();
        int puerto = Integer.parseInt(vistaOperador.getPuerto().trim());

        try {
            Socket socket = new Socket(ip, puerto);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(dni);
            out.close();
            socket.close();
            
            
        } catch (Exception e) {
            
        }
    
    }

