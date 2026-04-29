package net.salesianos.servidor;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler extends Thread {

    private final Socket socket;
    private final GameManager gameManager;
    private Jugador jugador;

    public ClientHandler(Socket socket, GameManager gameManager) {
        this.socket = socket;
        this.gameManager = gameManager;
    }

    @Override
    public void run() {
        try {
            DataOutputStream rawOut = new DataOutputStream(socket.getOutputStream());
            byte[] keyBytes = gameManager.getSecure().getKeyBytes();
            rawOut.writeInt(keyBytes.length);
            rawOut.write(keyBytes);
            rawOut.flush();
            System.out.println("[INFO] Clave AES enviada al cliente.");

            jugador = new Jugador("?", socket, gameManager.getSecure());
            String primerMensaje = jugador.getIn().readLine();

            if (primerMensaje == null || !primerMensaje.startsWith("NOMBRE:")) {
                jugador.enviar("ERROR:Se esperaba NOMBRE:<nombre>");
                jugador.cerrar();
                return;
            }

            String nombre = primerMensaje.substring(7).trim();
            jugador = new Jugador(nombre, socket, gameManager.getSecure());

            if (!gameManager.agregarJugador(jugador)) {
                jugador.enviar("ERROR:Sala llena o partida en curso");
                jugador.cerrar();
                return;
            }

            System.out.println(nombre + " se conectó");

            gameManager.broadcast("ESPERANDO:"
                    + gameManager.getNumJugadores()
                    + "/" + GameManager.MAX_JUGADORES);

            if (!gameManager.isPartidaEnCurso()
                    && gameManager.haySuficientesJugadores()) {
                Thread.sleep(2000);
                if (!gameManager.isPartidaEnCurso()) {
                    gameManager.iniciarPartida();
                }
            }

            String mensaje;
            while ((mensaje = jugador.getIn().readLine()) != null) {
                if (mensaje.equals("LANZAR")) {
                    gameManager.procesarLanzamiento(jugador);
                }
            }

        } catch (IOException e) {
            System.out.println("Conexión perdida: "
                    + (jugador != null ? jugador.getNombre() : "desconocido"));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            if (jugador != null) {
                gameManager.eliminarJugador(jugador);
                jugador.cerrar();
                System.out.println(jugador.getNombre() + " desconectado");
            }
        }
    }
}