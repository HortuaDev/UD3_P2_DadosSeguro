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

    public SecureManager() {
        this.secretKey = generateKey();
        this.cipher = initCipher(Cipher.ENCRYPT_MODE, secretKey);
        this.decipher = initCipher(Cipher.DECRYPT_MODE, secretKey);
    }

    public SecureManager(byte[] keyBytes) {
        this.secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
        this.cipher = initCipher(Cipher.ENCRYPT_MODE, secretKey);
        this.decipher = initCipher(Cipher.DECRYPT_MODE, secretKey);
    }

    private SecretKey generateKey() {
        try {
            KeyGenerator kg = KeyGenerator.getInstance(ALGORITHM);
            kg.init(128);
            return kg.generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("No se pudo generar la clave AES", e);
        }
    }

    private Cipher initCipher(int mode, SecretKey key) {
        try {
            Cipher c = Cipher.getInstance(ALGORITHM);
            c.init(mode, key);
            return c;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
            throw new RuntimeException("No se pudo inicializar el cifrador AES", e);
        }
    }

}