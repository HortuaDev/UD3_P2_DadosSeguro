# UD3 – Práctica 2 – Seguridad en mi app cliente/servidor

## Descripción

Extensión de la práctica 1 (juego de dados multijugador) con
comunicación cifrada mediante **AES de 128 bits**.
Todos los mensajes entre cliente y servidor viajan cifrados;
el tráfico interceptado con Wireshark ya no es legible.

## Documentación

| Paso | Descripción                           | Documento                                                      |
| ---- | ------------------------------------- | -------------------------------------------------------------- |
| 1    | Wireshark sin cifrado                 | [docs/01-wireshark-antes.md](docs/01-wireshark-antes.md)       |
| 2    | Clase de encriptación `SecureManager` | [docs/02-clase-encriptacion.md](docs/02-clase-encriptacion.md) |
| 3    | Modificaciones cliente y servidor     | [docs/03-modificaciones.md](docs/03-modificaciones.md)         |
| 4    | Wireshark con cifrado activo          | [docs/04-wireshark-despues.md](docs/04-wireshark-despues.md)   |
| 5    | Esquema de seguridad basado en roles  | [docs/05-esquema-roles.md](docs/05-esquema-roles.md)           |

## Estructura del proyecto

```text
├── README.md
├── docs/
│   ├── 01-wireshark-antes.md
│   ├── 02-clase-encriptacion.md
│   ├── 03-modificaciones.md
│   ├── 04-wireshark-despues.md
│   └── 05-esquema-roles.md
└── src/
└── net/salesianos/
├── cliente/
│   ├── ClienteDados.java
│   └── EscuchadorServidor.java
├── servidor/
│   ├── ServidorDados.java
│   ├── GameManager.java
│   ├── ClientHandler.java
│   └── Jugador.java
└── utils/
└── SecureManager.java
```

## Algoritmo

- **Algoritmo:** AES (Advanced Encryption Standard)
- **Longitud de clave:** 128 bits
- **Clases Java:** `javax.crypto.Cipher`, `javax.crypto.KeyGenerator`,
  `javax.crypto.spec.SecretKeySpec`

## Compilar y ejecutar

```bash
# Compilar
javac -d bin src/net/salesianos/utils/SecureManager.java
javac -d bin -cp bin src/net/salesianos/servidor/*.java
javac -d bin -cp bin src/net/salesianos/cliente/*.java

# Servidor
java -cp bin net.salesianos.servidor.ServidorDados

# Clientes (mínimo 2 para iniciar partida)
java -cp bin net.salesianos.cliente.ClienteDados
```

## Protocolo de mensajes

Todos los mensajes viajan cifrados. El receptor los descifra
antes de procesarlos, por lo que la lógica del juego no cambia.

| Mensaje                    | Dirección          | Significado           |
| -------------------------- | ------------------ | --------------------- |
| `NOMBRE:Juan`              | Cliente → Servidor | Registro              |
| `ESPERANDO:2/4`            | Servidor → Todos   | Sala de espera        |
| `INICIO:Juan,Ana`          | Servidor → Todos   | Partida iniciada      |
| `TURNO:Juan`               | Servidor → Todos   | Turno del jugador     |
| `ES_TU_TURNO`              | Servidor → Jugador | Notificación directa  |
| `LANZAR`                   | Cliente → Servidor | Tirar dados           |
| `DADOS:3,5,2,6,1`          | Servidor → Todos   | Resultado dados       |
| `COMBINACION:Juan:TRIO+15` | Servidor → Todos   | Combinación detectada |
| `PUNTOS:Juan:32`           | Servidor → Todos   | Puntos de la ronda    |
| `MARCADOR:Juan:32,Ana:20`  | Servidor → Todos   | Marcador acumulado    |
| `FIN:Juan:95,Ana:87`       | Servidor → Todos   | Ranking final         |
| `DESCONECTADO:Ana`         | Servidor → Todos   | Jugador abandonó      |
| `ERROR:mensaje`            | Servidor → Cliente | Error                 |
