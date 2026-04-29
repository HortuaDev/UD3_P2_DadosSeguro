[ <- Volver al inicio](/README.md)

# Paso 3 – Modificaciones en cliente y servidor

## Archivos modificados

| Archivo                           | Tipo de cambio                                                |
| --------------------------------- | ------------------------------------------------------------- |
| `utils/SecureManager.java`        | **Nuevo** – lógica de cifrado AES                             |
| `servidor/GameManager.java`       | Añadido campo `SecureManager` y getter                        |
| `servidor/Jugador.java`           | Streams cambiados, `enviar()` cifra, nuevo `recibirMensaje()` |
| `servidor/ClientHandler.java`     | Añadido handshake, constructores y lecturas actualizados      |
| `cliente/ClienteDados.java`       | Recibe clave, envía mensajes cifrados                         |
| `cliente/EscuchadorServidor.java` | Lee y descifra mensajes con `DataInputStream`                 |

---

## Handshake: cómo se comparte la clave

Al conectar un cliente, lo primero que ocurre es el intercambio de clave.
Sin este paso no hay comunicación posible.

```text
Cliente                                    Servidor
   |                                          |
   |---- TCP connect() ---------------------->|
   |                                          | GameManager tiene SecureManager
   |<--- int (16) + 16 bytes (clave) ---------| ClientHandler envía la clave
   |      SecureManager(keyBytes)             |
   |                                          |
   |---- int(len) + cifrado("NOMBRE:X") ----->| descifra → "NOMBRE:X"
   |<--- int(len) + cifrado("ESPERANDO:1/4")--|
   |                                          |
   |     .... resto del juego cifrado ....    |
```

---

## Formato de cada mensaje en el socket

```text
┌──────────────────┬──────────────────────────┐
│  4 bytes (int)   │     N bytes cifrados     │
│  longitud del    │         (AES)            │
│  payload         │                          │
└──────────────────┴──────────────────────────┘

Se usa `DataOutputStream.writeInt()` para la longitud
y `DataInputStream.readFully()` para leer exactamente esos bytes.
```

---

## Cambios en el servidor

### `GameManager`

````java
// Campo nuevo
private final SecureManager secure = new SecureManager();

// Getter para que ClientHandler pueda enviar la clave
public SecureManager getSecure() { return secure; }


### `Jugador`

```java
// Constructor actualizado
public Jugador(String nombre, Socket socket, SecureManager secure)

// enviar() ahora cifra
public void enviar(String mensaje) {
    byte[] cipherBytes = secure.encrypt(mensaje);
    out.writeInt(cipherBytes.length);
    out.write(cipherBytes);
    out.flush();
}

// recibirMensaje() descifra automáticamente
public String recibirMensaje() throws IOException {
    int length = in.readInt();
    byte[] cipherBytes = new byte[length];
    in.readFully(cipherBytes);
    return secure.decrypt(cipherBytes);
}
````

### `ClientHandler`

```java
// Handshake al inicio del run()
DataOutputStream rawOut = new DataOutputStream(socket.getOutputStream());
byte[] keyBytes = gameManager.getSecure().getKeyBytes();
rawOut.writeInt(keyBytes.length);
rawOut.write(keyBytes);
rawOut.flush();

// Lectura actualizada
String primerMensaje = jugador.recibirMensaje();
while ((mensaje = jugador.recibirMensaje()) != null) { ... }
```

---

## Cambios en el cliente

### `ClienteDados`

```java
// Recibir clave
int keyLength = in.readInt();
byte[] keyBytes = new byte[keyLength];
in.readFully(keyBytes);
SecureManager secure = new SecureManager(keyBytes);

// Enviar cifrado
byte[] cipherBytes = secure.encrypt(msg);
out.writeInt(cipherBytes.length);
out.write(cipherBytes);
out.flush();
```

### `EscuchadorServidor`

```java
// Bucle de lectura con descifrado
int length = in.readInt();
byte[] cipherBytes = new byte[length];
in.readFully(cipherBytes);
String mensaje = secure.decrypt(cipherBytes);
procesarMensaje(mensaje); // igual que antes
```

---

## Compilar y ejecutar

```bash
# Compilar
javac -d bin src/net/salesianos/utils/SecureManager.java
javac -d bin -cp bin src/net/salesianos/servidor/*.java
javac -d bin -cp bin src/net/salesianos/cliente/*.java

# Servidor
java -cp bin net.salesianos.servidor.ServidorDados

# Clientes (mínimo 2)
java -cp bin net.salesianos.cliente.ClienteDados
```

[ <- Volver al inicio](/README.md)
