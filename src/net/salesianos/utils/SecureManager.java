package net.salesianos.utils;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.util.Base64;

public class SecureManager {

    private static final String ALGORITHM = "AES";

    private final Cipher cipher;
    private final Cipher decipher;
    private final SecretKey secretKey;

    private SecretKey generateKey() {
        try {
            KeyGenerator kg = KeyGenerator.getInstance(ALGORITHM);
            kg.init(128);
            return kg.generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("No se pudo generar la clave AES", e);
        }
    }

}