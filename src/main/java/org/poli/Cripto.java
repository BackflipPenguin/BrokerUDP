package org.poli;

import java.security.*;

public class Cripto {
    /*private final KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");

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
}
