package org.poli;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

@FunctionalInterface
public interface CommandHandler {
    String handler(String[] sections, Mensaje mensaje, Topico caller) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException;

}
