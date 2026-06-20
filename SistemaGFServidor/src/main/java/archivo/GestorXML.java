package archivo;

import java.io.File;
import java.io.IOException;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import jakarta.xml.bind.Unmarshaller;

import persistencia.EstadoSistema;

public class GestorXML implements GestorArchivo {

    private final String archivoxml;

    public GestorXML(String nombre) {
        this.archivoxml = "estadoSistema_" + nombre + ".xml";
    }

    @Override
    public synchronized EstadoSistema leerArchivo() throws IOException 
    {
        File archivoXml = new File(archivoxml);

        if (!archivoXml.exists()) 
            return new EstadoSistema();

        try {
            JAXBContext contexto = JAXBContext.newInstance(EstadoSistema.class);
            Unmarshaller unmarshaller = contexto.createUnmarshaller();
            EstadoSistema estadoSis = (EstadoSistema) unmarshaller.unmarshal(archivoXml);
            if (estadoSis == null) {
                System.getLogger(GestorXML.class.getName()).log(System.Logger.Level.WARNING, "XML inválido. Retornando EstadoSistema vacío.");
                return new EstadoSistema();
            }
            return estadoSis;
        } catch (JAXBException e) {
            throw new IOException("Error al deserializar XML desde " + archivoxml, e);
        }
    }

    @Override
    public synchronized void guardarArchivo(EstadoSistema estado) throws IOException 
    {
        if (estado == null) {
            System.getLogger(GestorXML.class.getName()).log(System.Logger.Level.WARNING, "Intento de guardar EstadoSistema nulo");
            return;
        }
        try {
            JAXBContext contexto = JAXBContext.newInstance(EstadoSistema.class);
            Marshaller marshaller = contexto.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(estado, new File(archivoxml));
        } catch (JAXBException e) {
            throw new IOException("Error al serializar XML a " + archivoxml, e);
        }
    }

}
