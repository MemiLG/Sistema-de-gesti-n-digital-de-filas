package negocio;

import java.util.HashMap;

import persistencia.AdministradorPersistencia;
import persistencia.EstadoSistema;

public class GestorPS {

    private AdministradorPersistencia adminPersistencia;

    public GestorPS(){}

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
            this.adminPersistencia.guardarHistorial(historial);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            throw new RuntimeException( e);
        }
    }


    public void cargaEstadoInicial(ColaIngreso colaIng, Historial historial, HashMap<String,String> intentosRenotificacion, HashMap<String,String> puestoEnRenotificacion, Integer puerto) 
    {
        if (colaIng == null) 
        {
            throw new IllegalArgumentException();
        }

        if (historial == null) 
        {
            throw new IllegalArgumentException();
        }
        
        System.out.println("ESTADI INICIAL");
        String tipoAlmacenamiento = Configuracion.getFormatoAlmacenamiento();
        this.adminPersistencia = new AdministradorPersistencia(tipoAlmacenamiento, puerto);
        EstadoSistema estado = adminPersistencia.cargarEstadoSistema();

        if (estado != null){

            // Reconstruye la cola
            if (estado.getColaEspera() != null) 
            {
                for (String dni : estado.getColaEspera())
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
                for (HashMap.Entry<String, String> entry : estado.getIntentosRenotificacion().entrySet()) 
                {
                    if (entry.getKey() != null && entry.getValue() != null) 
                    {
                        intentosRenotificacion.put(entry.getKey(), entry.getValue());
                    }
                }
            }

            if (estado.getPuestoEnRenotificacion() != null) 
            {
                for (HashMap.Entry<String, String> entry : estado.getPuestoEnRenotificacion().entrySet()) 
                {
                    if (entry.getKey() != null && entry.getValue() != null) 
                    {
                        puestoEnRenotificacion.put(entry.getKey(), entry.getValue());
                    }
                }
            }
        }

        
    }


    public void guardarEstadoRenotificacion(HashMap<String,String>  intentosRenotificacion, HashMap<String,String> puestoEnRenotificacion) 
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

    public void tipoArchivo(String tipo, Integer puerto) 
    {
        try 
        {
            System.out.println("TIPO DE ARCHIVO");
            Configuracion.setFormatoAlmacenamiento(tipo);
            this.adminPersistencia = new AdministradorPersistencia(tipo, puerto); 
            
        } catch (Exception e) {
            System.err.println(e.getMessage());
            throw new RuntimeException( e);
        }
    }


}
