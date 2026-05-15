package main;

import java.io.IOException;
import javax.swing.JOptionPane;
import servidor.Servidor;
import vistas.InterfazServidor;


public class Main {
    public static void main(String[] args) {

        // Abrir la interfaz visual del servidor
        java.awt.EventQueue.invokeLater(() -> {
            new InterfazServidor().setVisible(true);
        });

        // Iniciar los dos hilos de servidores
        Servidor servidorPrincipal = new Servidor(1234);
        Servidor servidorRespaldo  = new Servidor(1235);

        //new Thread(servidorPrincipal).start();
        //new Thread(servidorRespaldo).start(); Hay que hacer que el servidor sea Hilo

        // Lanzar el Monitor como proceso separado
        try {
            new ProcessBuilder("java", "-cp", System.getProperty("java.class.path"), "monitor.Monitor")
                .inheritIO()
                .start();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null,
                "No se pudo iniciar el Monitor.",
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
