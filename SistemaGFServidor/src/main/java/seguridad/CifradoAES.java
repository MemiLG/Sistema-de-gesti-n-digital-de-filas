package seguridad;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class CifradoAES implements IEncripta {
    
    private Cipher cifrado = null;
    private SecretKey llave;
    IvParameterSpec ivFijo;
    
    public CifradoAES(SecretKey llave){
        try {
            cifrado = Cipher.getInstance("AES/CBC/NoPadding");
            this.llave = llave;
            byte[] ivBytes = Arrays.copyOf(this.llave.getEncoded(), 16);
            this.ivFijo = new IvParameterSpec(ivBytes);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException ex) {
            System.getLogger(CifradoAES.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }

    @Override
    public String encriptar(String mensaje) {
        try {
            cifrado.init(Cipher.ENCRYPT_MODE,this.llave,this.ivFijo);
            byte[] mensaje_encript = cifrado.doFinal(mensaje.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(mensaje_encript);
        } catch (InvalidKeyException | InvalidAlgorithmParameterException | UnsupportedEncodingException | IllegalBlockSizeException | BadPaddingException ex) {
            System.getLogger(CifradoAES.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
        return "";
    }

    @Override
    public String desencriptar(String mensaje) {
       try{
           byte[] mensaje_decript = Base64.getDecoder().decode(mensaje);
           this.cifrado.init(Cipher.DECRYPT_MODE, this.llave, this.ivFijo);
           return new String (this.cifrado.doFinal(mensaje_decript), "UTF-8");    
        } catch (InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException | BadPaddingException | UnsupportedEncodingException ex) {
            System.getLogger(CifradoAES.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
        return "";
    }
    
}
