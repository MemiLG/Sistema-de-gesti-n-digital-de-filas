package factorySeguridad;

import javax.crypto.SecretKey;
import seguridad.*;

public class CifradoFactory {
    
    private SecretKey llave;
    
    public CifradoFactory(SecretKey llave){
        this.llave = llave;
    }
    
    public IEncripta getCifrado(String cifrado){
        IEncripta aux = null;
        switch(cifrado){
            case("AES")->{
                    aux = new CifradoAES(this.llave);
                }
                case("DES")->{
                    aux = new CifradoDES(this.llave);
                }
                case("Blowfish")->{
                    aux = new CifradoBlowfish(this.llave);
                }
        }
        return aux;
    }
}
