package net.salesianos.cliente;

import net.salesianos.utils.SecureManager;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class ClienteDados {

    private static final String HOST = "localhost";
    private static final int PUERTO = 5000;

    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        try (Socket socket = new Socket(HOST, PUERTO)) {

            System.out.println("Conectado al servidor");

            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            DataInputStream in = new DataInputStream(socket.getInputStream());

            int keyLength = in.readInt();
            byte[] keyBytes = new byte[keyLength];
            in.readFully(keyBytes);
            SecureManager secure = new SecureManager(keyBytes);
            System.out.println("[INFO] Clave AES recibida y configurada.");

            EscuchadorServidor escuchador = new EscuchadorServidor(in, secure);
            escuchador.start();

            System.out.print("Tu nombre: ");
            String nombre = scanner.nextLine().trim();
            sendEncrypted(out, secure, "NOMBRE:" + nombre);

            while (!escuchador.isPartidaTerminada()) {
                if (escuchador.isEsMiTurno()) {
                    scanner.nextLine();
                    escuchador.setEsMiTurno(false);
                    sendEncrypted(out, secure, "LANZAR");
                } else {
                    Thread.sleep(100);
                }
            }

            System.out.println("Partida terminada. Hasta luego.");

        } catch (IOException e) {
            System.err.println("No se pudo conectar: " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            scanner.close();
        }
    }

    static void sendEncrypted(DataOutputStream out, SecureManager secure, String msg)
            throws IOException {
        byte[] cipherBytes = secure.encrypt(msg);
        out.writeInt(cipherBytes.length);
        out.write(cipherBytes);
        out.flush();
    }
}