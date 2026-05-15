package monitor;

import java.net.Socket;


public class ConexionServidor {
    private int puerto;
    private Socket socket;
    private int estado;

    public ConexionServidor(int puerto, Socket socket) {
        this.puerto = puerto;
        this.socket = socket;
    }

    public int getPuerto() {
        return puerto;
    }

    public Socket getSocket() {
        return socket;
    }

    public int getEstado() {
        return estado;
    }

    public void setPuerto(int puerto) {
        this.puerto = puerto;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }
    
    
}
