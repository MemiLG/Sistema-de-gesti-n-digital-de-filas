package timbre;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

import monitor.MonitorApp;

public class SonidoApp {

    public SonidoApp() {
    }

    private  Clip clipAct = null; 

        public  void reproducir(String rutaArchivo) 
    {

        try {

            System.out.println("Intentando reproducir: " + rutaArchivo);
            if (clipAct != null && clipAct.isOpen()) 
                clipAct.close();
        
            var recurso = MonitorApp.class.getResourceAsStream("/" + rutaArchivo);
            AudioInputStream audio = AudioSystem.getAudioInputStream(recurso);
            clipAct = AudioSystem.getClip();
            clipAct.open(audio);
            clipAct.start();

        } catch (Exception e) {
            System.err.println("Error reproduciendo sonido: " + e.getMessage());
            e.printStackTrace();
        }
    }



}
