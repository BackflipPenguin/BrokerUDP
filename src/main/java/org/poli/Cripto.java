package org.poli;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;

public class Cripto {
    private PrivateKey privateKey;
    private PublicKey publicKey;
    Signature dsa;

    public Cripto() throws NoSuchAlgorithmException{
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(1024);
        KeyPair pair = keyGen.generateKeyPair();
        this.privateKey = pair.getPrivate();
        this.publicKey = pair.getPublic();
        dsa = Signature.getInstance("SHA256withRSA");
    }

    public String generarFirma(byte[] mensaje) throws SignatureException {
        try {
            dsa.initSign(privateKey);
            dsa.update(mensaje);
            var s = dsa.sign();

            return Base64.getEncoder().encodeToString(s);

        } catch (Exception e){
            System.out.println("Error generando firma");
            e.printStackTrace();
        }
        return "";
   }

   public boolean verificarFirma(String mensaje, String firma, PublicKey autor) throws InvalidKeyException, SignatureException {
       var firmaDecoded = Base64.getDecoder().decode(firma.getBytes());

       dsa.initVerify(autor);
        dsa.update(mensaje.getBytes(StandardCharsets.UTF_8));
        return dsa.verify(firmaDecoded);
   }

    public byte[] encriptar(String mensaje, PublicKey pubKeyDestino) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher encryptCipher = Cipher.getInstance("RSA");
        encryptCipher.init(Cipher.ENCRYPT_MODE, pubKeyDestino);
        byte [] mensajeBytes = mensaje.getBytes(StandardCharsets.UTF_8);
        // System.out.println(Arrays.toString(mensajeEncriptadoBytes));
        return encryptCipher.doFinal(mensajeBytes);
    }
    public String desencriptar(byte[] mensajeEncriptadoBytes) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher decryptCipher = Cipher.getInstance("RSA");
        decryptCipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] mensajeDesencriptadoBytes = decryptCipher.doFinal(mensajeEncriptadoBytes);
        return new String(mensajeDesencriptadoBytes, StandardCharsets.UTF_8);
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public static void main(String[] args) throws NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, SignatureException {
        String mensaje = "ANASHEX";
        Cripto cripto1 = new Cripto();
        Cripto cripto2 = new Cripto();

        byte[] mensajeEncriptado = cripto1.encriptar("Hola perro", cripto2.getPublicKey());
        System.out.println(cripto2.desencriptar(mensajeEncriptado));
        var firma = cripto1.generarFirma(mensaje.getBytes(StandardCharsets.UTF_8));
        System.out.println(cripto2.verificarFirma(mensaje, firma, cripto1.getPublicKey()));
    }
}
