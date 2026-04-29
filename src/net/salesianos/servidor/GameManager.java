package net.salesianos.servidor;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.salesianos.utils.SecureManager;

public class GameManager {

    public static final int MIN_JUGADORES = 2;
    public static final int MAX_JUGADORES = 4;
    public static final int TOTAL_RONDAS = 5;

    private final List<Jugador> jugadores = new ArrayList<>();
    private int rondaActual = 0;
    private int turnoActual = 0;
    private boolean partidaEnCurso = false;
    private final Random random = new Random();

    private final SecureManager secure = new SecureManager();

    public synchronized boolean agregarJugador(Jugador jugador) {
        if (jugadores.size() >= MAX_JUGADORES || partidaEnCurso)
            return false;
        jugadores.add(jugador);
        return true;
    }

    public synchronized void eliminarJugador(Jugador jugador) {
        jugadores.remove(jugador);
        broadcast("DESCONECTADO:" + jugador.getNombre());
        if (turnoActual >= jugadores.size() && !jugadores.isEmpty()) {
            turnoActual = 0;
        }
    }

    public synchronized boolean haySuficientesJugadores() {
        return jugadores.size() >= MIN_JUGADORES;
    }

    public synchronized boolean isPartidaEnCurso() {
        return partidaEnCurso;
    }

    public synchronized int getNumJugadores() {
        return jugadores.size();
    }

    public synchronized void broadcast(String mensaje) {
        System.out.println("[BROADCAST] " + mensaje);
        for (Jugador j : jugadores) {
            j.enviar(mensaje);
        }
    }

    // Metodos del juego

    private int calcularSuma(int[] dados) {
        int suma = 0;
        for (int d : dados)
            suma += d;
        return suma;
    }

    private int calcularBonus(int[] dados) {
        int[] freq = new int[7];
        for (int d : dados)
            freq[d]++;

        int maxFreq = 0, pares = 0;
        for (int i = 1; i <= 6; i++) {
            if (freq[i] > maxFreq)
                maxFreq = freq[i];
            if (freq[i] >= 2)
                pares++;
        }

        if (maxFreq == 5)
            return 50; // Generala
        if (maxFreq == 4)
            return 35; // Póker
        if (maxFreq == 3 && pares == 2)
            return 25; // Full
        if (esEscalera(freq))
            return 20; // Escalera
        if (maxFreq == 3)
            return 15; // Trío
        if (pares >= 2)
            return 10; // Doble par
        if (pares == 1)
            return 5; // Par
        return 0;
    }

    private String detectarCombinacion(int[] dados) {
        int[] freq = new int[7];
        for (int d : dados)
            freq[d]++;

        int maxFreq = 0, pares = 0;
        for (int i = 1; i <= 6; i++) {
            if (freq[i] > maxFreq)
                maxFreq = freq[i];
            if (freq[i] >= 2)
                pares++;
        }

        if (maxFreq == 5)
            return "GENERALA +50";
        if (maxFreq == 4)
            return "POKER +35";
        if (maxFreq == 3 && pares == 2)
            return "FULL +25";
        if (esEscalera(freq))
            return "ESCALERA +20";
        if (maxFreq == 3)
            return "TRIO +15";
        if (pares >= 2)
            return "DOBLE PAR +10";
        if (pares == 1)
            return "PAR +5";
        return "";
    }

    private boolean esEscalera(int[] freq) {
        boolean e1 = true, e2 = true;
        for (int i = 1; i <= 5; i++)
            if (freq[i] == 0)
                e1 = false;
        for (int i = 2; i <= 6; i++)
            if (freq[i] == 0)
                e2 = false;
        return e1 || e2;
    }

    private String arrayToString(int[] arr) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arr.length; i++) {
            if (i > 0)
                sb.append(",");
            sb.append(arr[i]);
        }
        return sb.toString();
    }

    private String getMarcador() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < jugadores.size(); i++) {
            if (i > 0)
                sb.append(",");
            Jugador j = jugadores.get(i);
            sb.append(j.getNombre()).append(":").append(j.getPuntuacion());
        }
        return sb.toString();
    }

    public synchronized void iniciarPartida() {
        partidaEnCurso = true;
        rondaActual = 1;
        turnoActual = 0;

        StringBuilder nombres = new StringBuilder();
        for (int i = 0; i < jugadores.size(); i++) {
            if (i > 0)
                nombres.append(",");
            nombres.append(jugadores.get(i).getNombre());
        }

        broadcast("INICIO:" + nombres);
        iniciarRonda();
    }

    private void iniciarRonda() {
        broadcast("RONDA:" + rondaActual + "/" + TOTAL_RONDAS);
        anunciarTurno();
    }

    private void anunciarTurno() {
        if (jugadores.isEmpty())
            return;
        Jugador actual = jugadores.get(turnoActual);
        broadcast("TURNO:" + actual.getNombre());
        actual.enviar("ES_TU_TURNO"); // mensaje directo solo a él
    }

    public synchronized void procesarLanzamiento(Jugador jugador) {
        if (!jugadores.get(turnoActual).equals(jugador)) {
            jugador.enviar("ERROR:No es tu turno");
            return;
        }

        int[] dados = new int[5];
        for (int i = 0; i < 5; i++)
            dados[i] = random.nextInt(6) + 1;

        int suma = calcularSuma(dados);
        int bonus = calcularBonus(dados);
        String combinacion = detectarCombinacion(dados);
        int total = suma + bonus;

        jugador.sumarPuntos(total);

        broadcast("DADOS:" + arrayToString(dados));
        if (!combinacion.isEmpty()) {
            broadcast("COMBINACION:" + jugador.getNombre() + ":" + combinacion);
        }
        broadcast("PUNTOS:" + jugador.getNombre() + ":" + total);
        broadcast("MARCADOR:" + getMarcador());

        turnoActual++;
        if (turnoActual >= jugadores.size()) {
            turnoActual = 0;
            if (rondaActual >= TOTAL_RONDAS) {
                finalizarPartida();
            } else {
                rondaActual++;
                iniciarRonda();
            }
        } else {
            anunciarTurno();
        }
    }

    private void finalizarPartida() {
        jugadores.sort((a, b) -> b.getPuntuacion() - a.getPuntuacion());

        StringBuilder ranking = new StringBuilder();
        for (int i = 0; i < jugadores.size(); i++) {
            if (i > 0)
                ranking.append(",");
            Jugador j = jugadores.get(i);
            ranking.append(j.getNombre()).append(":").append(j.getPuntuacion());
        }

        broadcast("FIN:" + ranking);

        for (Jugador j : jugadores)
            j.cerrar();
        jugadores.clear();
        partidaEnCurso = false;
    }

    // Metodo Secure

    public SecureManager getSecure() {
        return secure;
    }
}