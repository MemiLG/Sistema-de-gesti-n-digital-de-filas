package negocio;

import java.util.HashMap;

import persistencia.AdministradorPersistencia;
import persistencia.EstadoSistema;

public class GestorPS {

    private AdministradorPersistencia adminPersistencia;

    public GestorPS() 
    {
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


    public void cargaEstadoInicial(ColaIngreso colaIng, Historial historial, HashMap<Integer,Integer> intentosRenotificacion, HashMap<Integer,String> puestoEnRenotificacion) 
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

            if (estado.getIntentosRenotificacion() != null) 
            {
                for (HashMap.Entry<Integer, Integer> entry : estado.getIntentosRenotificacion().entrySet()) 
                {
                    if (entry.getKey() != null && entry.getValue() != null) 
                    {
                        intentosRenotificacion.put(entry.getKey(), entry.getValue());
                    }
                }
            }

            if (estado.getPuestoEnRenotificacion() != null) 
            {
                for (HashMap.Entry<Integer, String> entry : estado.getPuestoEnRenotificacion().entrySet()) 
                {
                    if (entry.getKey() != null && entry.getValue() != null) 
                    {
                        puestoEnRenotificacion.put(entry.getKey(), entry.getValue());
                    }
                }
            }

        } catch (Exception e) {
            System.out.println( e.getMessage());
        }
    }


    public void guardarEstadoRenotificacion(HashMap<Integer,Integer>  intentosRenotificacion, HashMap<Integer,String> puestoEnRenotificacion) 
    {
        if (intentosRenotificacion == null) 
            throw new IllegalArgumentException();

        if (puestoEnRenotificacion == null) 
            throw new IllegalArgumentException();
        
        try 
        {
            adminPersistencia.guardarIntentosRenotificacion(intentosRenotificacion, puestoEnRenotificacion);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            throw new RuntimeException( e);
        }
    }

    public void tipoArchivo(String tipo) 
    {
        try 
        {
            this.adminPersistencia = new AdministradorPersistencia(tipo, adminPersistencia.getIdServidor());
        } catch (Exception e) {
            System.err.println(e.getMessage());
            throw new RuntimeException( e);
        }
    }
}
