package factorySeguridad;

import java.security.NoSuchAlgorithmException;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

public class LlaveFactory {
    
    public LlaveFactory(){
        super();
    }
    
    public SecretKey getLlave(String cifrado){
        SecretKey aux = null;
        try{
            switch(cifrado){
                case("AES")->{
                    KeyGenerator keyGen = KeyGenerator.getInstance("AES");
                    keyGen.init(256);
                    aux = keyGen.generateKey();
                }
                case("DES")->{
                    KeyGenerator keyGen = KeyGenerator.getInstance("DES");
                    keyGen.init(56);
                    aux = keyGen.generateKey();
                }
                case("Blowfish")->{
                    KeyGenerator keyGen = KeyGenerator.getInstance("Blowfish");
                    keyGen.init(256);
                    aux = keyGen.generateKey();
                }
            } 
            return aux;
        } catch(NoSuchAlgorithmException ex){
            System.getLogger(LlaveFactory.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
        return aux;
    }
}
