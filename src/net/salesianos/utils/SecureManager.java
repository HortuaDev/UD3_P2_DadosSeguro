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

}