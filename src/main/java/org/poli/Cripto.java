package org.poli;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;

public class Cripto {
    private PrivateKey privateKey;
    private PublicKey publicKey;
    Signature dsa;

    public Cripto() {
        KeyPair pair = null;
        try{
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(1024);
            pair = keyGen.generateKeyPair();
            this.privateKey = pair.getPrivate();
            this.publicKey = pair.getPublic();
            dsa = Signature.getInstance("SHA256withRSA");
      } catch (NoSuchAlgorithmException e) {
            System.out.println("[CRIPTO] NO EXISTE EL ALGORITMO");
            return;
        }
    }
    public SecretKey generarSecreto() {
        KeyGenerator symKeyGen = null;
        try {
            symKeyGen = KeyGenerator.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            System.out.println("[CRIPTO] NO EXISTE EL ALGORITMO");
            return null;
        }
        symKeyGen.init(256);
        return  symKeyGen.generateKey();
    }

    public String generarFirma(byte[] mensaje) {
        try {
            dsa.initSign(privateKey);
            dsa.update(mensaje);
            var s = dsa.sign();

            return Base64.getEncoder().encodeToString(s);

        } catch (Exception e) {
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

    public EncriptedResult encriptar(byte[] input, SecretKey key){
        try {
            byte[] ivBytes = new byte[16];
            new SecureRandom().nextBytes(ivBytes);
            IvParameterSpec iv = new IvParameterSpec(ivBytes);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key, iv);
            System.out.println("[CRIPTO] ENCRIPTANDO: ");
            System.out.println("[CRIPTO] INPUT: " + new String(input, StandardCharsets.UTF_8));
            System.out.println("[CRIPTO] IV: " + Base64.getEncoder().encodeToString(ivBytes));
            return new EncriptedResult(
                    Base64.getEncoder().encode(ivBytes),
                    Base64.getEncoder().encode(cipher.doFinal(input)));
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidAlgorithmParameterException |
                 InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            System.out.println("[CRIPTO] ERROR DE ENCRIPTACION");
            return null;
        }
    }

    public byte[] desencriptar(EncriptedResult er, SecretKey key) {
        try {
            System.out.println("[CRIPTO] " + new String(Base64.getDecoder().decode(er.getIv()), StandardCharsets.UTF_8));
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            IvParameterSpec iv = new IvParameterSpec(Base64.getDecoder().decode(er.getIv()));
            cipher.init(Cipher.DECRYPT_MODE, key, iv);
            return cipher.doFinal(Base64.getDecoder().decode(er.getMessage()));
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidAlgorithmParameterException |
                 InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            System.out.println("[CRIPTO] Error desencriptando.");
            return null;
        }
    }
    public byte[] encriptar(byte[] mensajeBytes, PublicKey pubKeyDestino) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher encryptCipher = Cipher.getInstance("RSA");
        encryptCipher.init(Cipher.ENCRYPT_MODE, pubKeyDestino);
//        byte [] mensajeBytes = mensaje.getBytes(StandardCharsets.UTF_8);
        // System.out.println(Arrays.toString(mensajeEncriptadoBytes));
        return Base64.getEncoder().encode(encryptCipher.doFinal(mensajeBytes));
    }

    public byte[] desencriptar(byte[] mensajeCodificadoBytes) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher decryptCipher = Cipher.getInstance("RSA");
        decryptCipher.init(Cipher.DECRYPT_MODE, privateKey);
        var mensajeEncriptadoBytes = Base64.getDecoder().decode(mensajeCodificadoBytes);
        return decryptCipher.doFinal(mensajeEncriptadoBytes);
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }
}
