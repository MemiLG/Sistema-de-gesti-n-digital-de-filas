package negocio;

import persistencia.AdministradorPersistencia;
import persistencia.EstadoSistema;

public class GestorPS {

    private AdministradorPersistencia adminPersistencia;

    public GestorPS() 
    {
        this.adminPersistencia = new AdministradorPersistencia();
    }

    public void RCPersistencia(ColaIngreso cola)
    {
        if (cola == null) {
            throw new IllegalArgumentException();
        }
        try {
            adminPersistencia.guardarColaIngreso(cola);
        } catch (Exception e) {
            System.err.println( e.getMessage());
            throw new RuntimeException( e);
        }
    }

    public void GHPersistencia(Historial historial)
    {
        if (historial == null) 
        {
            throw new IllegalArgumentException();
        }

        try {
            adminPersistencia.guardarHistorial(historial);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            throw new RuntimeException( e);
        }
    }


    public void ReintentosRenotificacion(int intentos, int dni) 
    {
        try {
            adminPersistencia.guardarIntentosRenotificacion(intentos, dni);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            throw new RuntimeException( e);
        }
    }
    
    public void SacarDNIRenotificacion(int dni) 
    {
        try {
            adminPersistencia.SacarIntentosRenotificacion(dni);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            throw new RuntimeException( e);
        }
    }

    public void cargaEstadoInicial(ColaIngreso colaIng, Historial historial) 
    {
        if (colaIng == null) 
        {
            throw new IllegalArgumentException();
        }

        if (historial == null) 
        {
            throw new IllegalArgumentException();
        }
        
        
        try 
        {
            EstadoSistema estado = adminPersistencia.cargarEstadoSistema();

            // Reconstruye la cola
            if (estado.getColaEspera() != null) 
            {
                for (Integer dni : estado.getColaEspera())
                {
                    if (dni != null) 
                    {
                        colaIng.nuevoIngreso(dni);
                    }
                }
            }

            // Reconstruye el historial
            if (estado.getHistorialLlamados() != null) 
            {
                for (String entrada : estado.getHistorialLlamados()) 
                {
                    if (entrada != null) 
                    {
                        historial.IngresoHistorial(entrada);
                    }
                }
            }

        } catch (Exception e) {
            System.out.println( e.getMessage());
        }
    }

    //REINTENTOS DE NOTIFICACIÓN

    public void cargaEstadoRenotificacion(java.util.Map<String, Integer> intentosRenotificacion)
    {
        if (intentosRenotificacion == null) 
        {
            throw new IllegalArgumentException();
        }
        
        try 
        {
            EstadoSistema estado = adminPersistencia.cargarEstadoSistema();

            // Reconstruye el mapa de intentos de renotificación
            if (estado.getIntentosRenotificacion() != null) 
            {
                for (java.util.Map.Entry<String, Integer> entry : estado.getIntentosRenotificacion().entrySet()) 
                {
                    String dni = entry.getKey();
                    Integer intentos = entry.getValue();
                    if (dni != null && intentos != null) 
                    {
                        intentosRenotificacion.put(dni, intentos);
                    }
                }
            }

        } catch (Exception e) {
            System.out.println( e.getMessage());
        }

    }
}
