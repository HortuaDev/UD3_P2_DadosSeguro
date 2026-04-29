package net.salesianos.cliente;

import net.salesianos.utils.SecureManager;

import java.io.DataInputStream;
import java.io.IOException;

public class EscuchadorServidor extends Thread {

    private final DataInputStream in;
    private final SecureManager secure;

    private volatile boolean partidaTerminada = false;
    private volatile boolean esMiTurno = false;

    public EscuchadorServidor(DataInputStream in, SecureManager secure) {
        this.in = in;
        this.secure = secure;
        setDaemon(true);
    }

    @Override
    public void run() {
        try {
            while (true) {
                int length = in.readInt();
                if (length <= 0)
                    break;
                byte[] cipherBytes = new byte[length];
                in.readFully(cipherBytes);
                String mensaje = secure.decrypt(cipherBytes);
                procesarMensaje(mensaje);
            }
        } catch (IOException e) {
            if (!partidaTerminada) {
                System.out.println("Conexión con servidor perdida.");
            }
        }
        partidaTerminada = true;
    }

    private void procesarMensaje(String mensaje) {

        if (mensaje.startsWith("ESPERANDO:")) {
            System.out.println("Esperando jugadores... ("
                    + mensaje.substring(10) + ")");

        } else if (mensaje.startsWith("INICIO:")) {
            System.out.println("\n¡Partida iniciada! Jugadores: "
                    + mensaje.substring(7));

        } else if (mensaje.startsWith("RONDA:")) {
            System.out.println("\n── RONDA " + mensaje.substring(6) + " ──");

        } else if (mensaje.startsWith("TURNO:")) {
            System.out.println("Turno de: " + mensaje.substring(6));

        } else if (mensaje.equals("ES_TU_TURNO")) {
            esMiTurno = true;
            System.out.println("¡ES TU TURNO! Pulsa ENTER para lanzar...");

        } else if (mensaje.startsWith("DADOS:")) {
            System.out.println("Dados: [" + mensaje.substring(6) + "]");

        } else if (mensaje.startsWith("COMBINACION:")) {
            String[] partes = mensaje.substring(12).split(":", 2);
            System.out.println(partes[0] + " → " + partes[1]);

        } else if (mensaje.startsWith("PUNTOS:")) {
            String[] partes = mensaje.substring(7).split(":");
            System.out.println(partes[0] + " obtiene " + partes[1] + " pts");

        } else if (mensaje.startsWith("MARCADOR:")) {
            System.out.println("Marcador: " + mensaje.substring(9));

        } else if (mensaje.startsWith("FIN:")) {
            System.out.println("\n=== FIN DE PARTIDA ===");
            String[] jugadores = mensaje.substring(4).split(",");
            String[] medallas = { "1.", "2.", "3.", "4." };
            for (int i = 0; i < jugadores.length; i++) {
                String[] kv = jugadores[i].split(":");
                System.out.println(medallas[i] + " "
                        + kv[0] + ": " + kv[1] + " pts");
            }
            partidaTerminada = true;

        } else if (mensaje.startsWith("DESCONECTADO:")) {
            System.out.println(mensaje.substring(13) + " se desconectó");

        } else if (mensaje.startsWith("ERROR:")) {
            System.out.println("Error: " + mensaje.substring(6));
        }
    }

    public boolean isPartidaTerminada() {
        return partidaTerminada;
    }

    public boolean isEsMiTurno() {
        return esMiTurno;
    }

    public void setEsMiTurno(boolean v) {
        esMiTurno = v;
    }
}
