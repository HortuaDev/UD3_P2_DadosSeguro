[ <- Volver al inicio](/README.md)

# Paso 2 – Clase de encriptación: `SecureManager`

## Ubicación

`src/net/salesianos/utils/SecureManager.java`

## Algoritmo: AES

| Parámetro         | Valor                              |
| ----------------- | ---------------------------------- |
| Algoritmo         | AES (Advanced Encryption Standard) |
| Longitud de clave | 128 bits (16 bytes)                |
| Clase Java        | `javax.crypto.Cipher`              |
| Generación clave  | `javax.crypto.KeyGenerator`        |

### ¿Por qué AES?

- Es el estándar simétrico más usado y auditado del mundo.
- Es muy rápido comparado con algoritmos asimétricos como RSA.
- Java lo incluye de serie en `javax.crypto`, sin librerías externas.

## Campos

```java
private static final String ALGORITHM = "AES";

private final Cipher    cipher;    // cifra mensajes salientes
private final Cipher    decipher;  // descifra mensajes entrantes
private final SecretKey secretKey; // clave AES de 128 bits
```

## Constructores

```java
// Servidor: genera una clave AES aleatoria nueva
public SecureManager()

// Cliente: reconstruye el SecureManager con la clave recibida del servidor
public SecureManager(byte[] keyBytes)
```

## Métodos

### `encrypt(String plainText) → byte[]`

Convierte el texto a bytes UTF-8 y los cifra con AES.
Devuelve los bytes cifrados.

```java
public byte[] encrypt(String plainText) {
    return cipher.doFinal(plainText.getBytes("UTF-8"));
}
```

### `decrypt(byte[] cipherBytes) → String`

Descifra los bytes recibidos y devuelve el texto original.

```java
public String decrypt(byte[] cipherBytes) {
    return new String(decipher.doFinal(cipherBytes), "UTF-8");
}
```

### `getKeyBytes() → byte[]`

Devuelve los bytes de la clave. El servidor los usa
para enviarlos al cliente en el handshake.

### `printEncrypted(byte[] cipherBytes)`

Muestra el mensaje cifrado en Base64 por consola. Útil para debug.

## Ejemplo de uso

```java
// Servidor genera la clave y cifra
SecureManager sm = new SecureManager();
byte[] cifrado = sm.encrypt("DADOS:3,5,2,6,1");

// Cliente recibe la clave y descifra
SecureManager smCliente = new SecureManager(sm.getKeyBytes());
String original = smCliente.decrypt(cifrado);
// original → "DADOS:3,5,2,6,1"
```

[ <- Volver al inicio](/README.md)
