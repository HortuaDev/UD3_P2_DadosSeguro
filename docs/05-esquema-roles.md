[ <- Volver al inicio](/README.md)

# Paso 5 – Esquema de seguridad basado en roles

## Contexto

Si el juego escalara a un proyecto real con usuarios registrados,
clasificaciones globales y administración, sería necesario controlar
qué puede hacer cada tipo de usuario.

## Roles definidos

| Rol         | Descripción                                              |
| ----------- | -------------------------------------------------------- |
| `INVITADO`  | No autenticado. Solo puede ver el lobby.                 |
| `JUGADOR`   | Registrado y autenticado. Puede unirse y jugar partidas. |
| `MODERADOR` | Puede expulsar jugadores y pausar partidas.              |
| `ADMIN`     | Control total: usuarios, roles, servidor y cifrado.      |

## Tabla de permisos

| Acción                | INVITADO | JUGADOR | MODERADOR | ADMIN |
| --------------------- | :------: | :-----: | :-------: | :---: |
| Ver lobby             |    ✅    |   ✅    |    ✅     |  ✅   |
| Unirse a partida      |    ❌    |   ✅    |    ✅     |  ✅   |
| Lanzar dados          |    ❌    |   ✅    |    ✅     |  ✅   |
| Ver marcador          |    ❌    |   ✅    |    ✅     |  ✅   |
| Expulsar jugador      |    ❌    |   ❌    |    ✅     |  ✅   |
| Pausar partida        |    ❌    |   ❌    |    ✅     |  ✅   |
| Ver logs del servidor |    ❌    |   ❌    |    ✅     |  ✅   |
| Gestionar usuarios    |    ❌    |   ❌    |    ❌     |  ✅   |
| Cambiar clave AES     |    ❌    |   ❌    |    ❌     |  ✅   |

## Jerarquía

```text
ADMIN
└── MODERADOR
└── JUGADOR
└── INVITADO
```

Cada rol hereda los permisos del nivel inferior.

## Implementación propuesta

### Enumerado de roles

```java
public enum Rol {
    INVITADO, JUGADOR, MODERADOR, ADMIN;

    public boolean tienePermisoSobre(Rol otroRol) {
        return this.ordinal() >= otroRol.ordinal();
    }
}
```

### Verificación en GameManager

```java
private void verificarRol(Jugador jugador, Rol rolMinimo) {
    if (!jugador.getRol().tienePermisoSobre(rolMinimo)) {
        jugador.enviar("ERROR:Permisos insuficientes");
        throw new SecurityException("Acceso denegado: " + jugador.getRol());
    }
}

public void expulsarJugador(Jugador solicitante, Jugador objetivo) {
    verificarRol(solicitante, Rol.MODERADOR);
    eliminarJugador(objetivo);
    broadcast("EXPULSADO:" + objetivo.getNombre());
}
```

### Autenticación en el handshake

```text
Cliente conecta → recibe clave AES
Cliente envía:  NOMBRE:<nombre>
Servidor envía: AUTH_REQUERIDA
Cliente envía:  LOGIN:<usuario>:<contraseña_SHA256>
Servidor valida → asigna rol → envía: ROL_ASIGNADO:JUGADOR
```

La contraseña nunca viaja en claro, se hashea con `SHA-256`
en el cliente antes de enviarla.

[ <- Volver al inicio](/README.md)
