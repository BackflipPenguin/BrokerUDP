package org.poli;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Arrays;

public class Cripto {
    /*
    private final KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");

    private KeyPair pair = generator.generateKeyPair();*/
    private PrivateKey privateKey;
    private PublicKey publicKey;

    public Cripto() throws NoSuchAlgorithmException{
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(1024);
        KeyPair pair = keyGen.generateKeyPair();
        this.privateKey = pair.getPrivate();
        this.publicKey = pair.getPublic();
    }

    // Falta hacer un metodo que encripte y otro que desencripte
    public byte[] encriptar(String mensaje) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher encryptCipher = Cipher.getInstance("RSA");
        encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte [] mensajeBytes = mensaje.getBytes(StandardCharsets.UTF_8);
        byte[] mensajeEncriptadoBytes =encryptCipher.doFinal(mensajeBytes);
        System.out.println(Arrays.toString(mensajeEncriptadoBytes));
        return mensajeEncriptadoBytes;
    }
    public void desencriptar(byte[] mensajeEncriptadoBytes) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher decryptCipher = Cipher.getInstance("RSA");
        decryptCipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] mensajeDesencriptadoBytes = decryptCipher.doFinal(mensajeEncriptadoBytes);
        String mensajeDesencriptado = new String(mensajeDesencriptadoBytes, StandardCharsets.UTF_8);
        System.out.println(mensajeDesencriptado);
    }
    public static void main(String[] args) throws NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        Cripto cripto = new Cripto();
        byte[] mensajeEncriptado = cripto.encriptar("Hola perro");
        cripto.desencriptar(mensajeEncriptado);
    }
}
