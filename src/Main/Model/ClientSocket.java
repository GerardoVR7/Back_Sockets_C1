package Main.Model;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Observable;

public class ClientSocket extends Observable implements Runnable {
    private Socket socket;
    private DataInputStream bufferEntrada = null;

    public ClientSocket(Socket socket){
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            bufferEntrada = new DataInputStream(socket.getInputStream());
            String mensaje = "";
            do {
                mensaje = bufferEntrada.readUTF();
                System.out.println("HILO"+ mensaje);
                this.setChanged();
                this.notifyObservers(mensaje);
            } while (!mensaje.equals("exit"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}