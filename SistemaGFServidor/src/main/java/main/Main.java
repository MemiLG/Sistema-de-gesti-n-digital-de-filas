package main;

import java.io.IOException;
import javax.swing.JOptionPane;
import servidor.Servidor;
import vistas.InterfazServidor;


public class Main {
    public static void main(String[] args) {

        // Iniciar los dos hilos de servidores
        Servidor servidorPrincipal = new Servidor(1234,1);
        Servidor servidorRespaldo  = new Servidor(1235,2);

        new Thread(servidorPrincipal).start();
        new Thread(servidorRespaldo).start();

    }
}
