package net.salesianos.servidor;

import net.salesianos.utils.SecureManager;

import java.io.*;
import java.net.Socket;

public class Jugador {

    private String nombre;
    private int puntuacion;
    private Socket socket;

    private DataOutputStream out;
    private DataInputStream in;
    private SecureManager secure;

    public Jugador(String nombre, Socket socket, SecureManager secure) throws IOException {
        this.nombre = nombre;
        this.puntuacion = 0;
        this.socket = socket;
        this.secure = secure;
        this.out = new DataOutputStream(socket.getOutputStream());
        this.in = new DataInputStream(socket.getInputStream());
    }

    public void cerrar() {
        try {
            if (!socket.isClosed())
                socket.close();
        } catch (IOException e) {
            System.err.println("Error cerrando socket: " + e.getMessage());
        }
    }

    public String getNombre() {
        return nombre;
    }

    public int getPuntuacion() {
        return puntuacion;
    }

    public DataOutputStream getOut() {
        return out;
    }

    public DataInputStream getIn() {
        return in;
    }

    public void sumarPuntos(int puntos) {
        this.puntuacion += puntos;
    }

    public void enviar(String mensaje) {
        try {
            byte[] cipherBytes = secure.encrypt(mensaje);
            out.writeInt(cipherBytes.length);
            out.write(cipherBytes);
            out.flush();
        } catch (IOException e) {
            System.err.println("Error enviando mensaje a " + nombre + ": " + e.getMessage());
        }
    }

    public String recibirMensaje() throws IOException {
        int length = in.readInt();
        if (length <= 0)
            return null;
        byte[] cipherBytes = new byte[length];
        in.readFully(cipherBytes);
        return secure.decrypt(cipherBytes);
    }
}