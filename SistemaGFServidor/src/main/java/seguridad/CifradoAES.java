package seguridad;

import java.security.NoSuchAlgorithmException;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class CifradoAES implements IEncripta {
    
    private Cipher cifrado = null;
    private SecretKey llave;
    
    public CifradoAES(SecretKey llave){
        try {
            cifrado = Cipher.getInstance("AES/CBC/NoPadding");
            this.llave = llave;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException ex) {
            System.getLogger(CifradoAES.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }

    @Override
    public void encriptar(String mensaje) {
        
    }

    @Override
    public void desencriptar(String mensaje) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
    
}
