package servidor;


public class ConstantesServidor {
    public static final String CLIENTE_VERIFICADO = "Cliente verificado correctamente";
    public static final String CLIENTE_YA_EXISTE = "Ya esta cargado en la lista de espera"; //Tiene que tratarlo la terminal
    public static final String CARGA_NUEVO_CLIENTE = "CARGA_NUEVO_CLIENTE";
    public static final String CLIENTE_CARGADO = "Se ha cargado correctamente"; //Tiene que tratarlo la terminal
    public static final String LLAMAR_SIGUIENTE = "LLAMAR_SIGUIENTE";
    public static final String RENOVAR_NOTIFICACION = "RE-NOTIFICAR";
    public static final String COLA_VACIA = "No hay clientes por atender"; //Tiene que tratarlo el puesto de atencion
    /** Mensaje al puesto: tamaño actual de la cola de espera (ej. COLA|3). */
    public static final String PREFIJO_TAMANO_COLA = "COLA|";
    public static final String PING = "PING";
    public static final String ECHO = "ECHO";
    public static final String ESTADO_INTERNO = "ESTADO_INTERNO";
    
}
