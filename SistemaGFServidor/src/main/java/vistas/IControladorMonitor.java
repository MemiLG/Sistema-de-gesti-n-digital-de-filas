package vistas;

public interface IControladorMonitor {
    public void cerrarConexion();
    public void establecerProcesos(Process procesoPrincipal, Process procesoSecundario);
    public boolean apagarServidorPrincipal();
    public boolean agregarServidorPasivo();
}
