package Main.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import Main.Model.ClientSocket;
import Main.Model.Nodo;
import Main.Model.Server;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

public class Controller implements Observer {

    ServerSocket serverSocket = null;
    private final int PORT = 3001;
    private ArrayList<Nodo> poolSocket = new ArrayList<>();
    private  ArrayList<String> historial = new ArrayList<>();
    @FXML
    private Button btnOpenServer;

    @FXML
    private Button btnSalir;

    @FXML
    private ListView<String> listClient;

    @FXML
    private Circle circleLed;

    @FXML
    void OpenServerOnMouseClicked(MouseEvent event) {
        byte[] ipBytes = {(byte) 192, (byte) 168, (byte) 0, (byte) 7};
        InetAddress ip = null;

        try {
            ip = InetAddress.getByAddress(ipBytes);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        try {
            serverSocket = new ServerSocket(PORT, 100, ip);
            listClient.getItems().add("Server abierto: " + serverSocket.getInetAddress().getHostName());
            circleLed.setFill(Color.GREEN);

            Server server = new Server(serverSocket);
            server.addObserver(this);
            new Thread(server).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
/*        finally {
            try {
                serverSocket.close();
                listClient.getItems().add("Server cerrado");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }*/

    }

    @FXML
    void SalirOnMouseClicked(MouseEvent event) {
        System.exit(1);
    }

    @Override
    public void update(Observable o, Object arg) {

        if (o instanceof Server) {
            Socket socket = (Socket) arg;
            System.out.println("PROCESO DE CONEXION");
            poolSocket.add(new Nodo(socket.hashCode(), "usuario"+ poolSocket.size(), socket));
            // Broadcast a todos los sockets conectados para actualizar la lista de conexiones
            broadCast();
            // Crear un hilo que reciba mensajes entrantes de ese nuevo socket creado
            ClientSocket clientSocket = new ClientSocket(socket);
            clientSocket.addObserver(this);
            new Thread(clientSocket).start();
            Platform.runLater(() -> listClient.getItems().add(socket.getInetAddress().getHostName()));
        }
        if (o instanceof ClientSocket) {
            String paquete_recibido = (String) arg;
            System.out.println("llego al servidor");
            System.out.println(paquete_recibido);
            String[] datagrama =  paquete_recibido.split(":");

            //System.out.println("tipo de envio: " + datagrama[0] + ":" + datagrama[1] + ":" + datagrama[2] + ":" + datagrama[3]);



            if (datagrama[0].equals("3")) {
                sendMessage(datagrama);
            }
            if ( datagrama[0].equals("2")){
                System.out.println("tipo de envio: " + datagrama[0] + ":" + datagrama[1] + ":" + datagrama[2] + ":" + datagrama[3] + ":" + datagrama[4] + ":" + datagrama[5] + ":" + datagrama[6]);
                sendImage(datagrama);
            }


            Platform.runLater(() -> listClient.getItems().add(paquete_recibido));

        }

        //Platform.runLater(() -> listClient.getItems().add(socket.getInetAddress().getHostName()));

    }




    private void broadCast() {
        DataOutputStream bufferDeSalida = null;
        Nodo ultimaConexion = poolSocket.get(poolSocket.size() - 1);
        System.out.println("PROCESO BROADCAST");
        String listaConexiones = createList();
        for (Nodo nodo : poolSocket) {
            try {

                bufferDeSalida = new DataOutputStream((nodo.getSocket().getOutputStream()));
                bufferDeSalida.flush();

                String tipoMensaje = "1:";
                String origen = "Servidor:";
                String paqueteServidor = tipoMensaje + origen + nodo.getName() +  listaConexiones;
                System.out.println(paqueteServidor);
                bufferDeSalida.writeUTF(paqueteServidor);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String createList(){
        String conexiones = " ";
        System.out.println("PROCESO CREATE LIST");
        for (Nodo nodo : poolSocket){
            conexiones=conexiones + ":" + nodo.getName();
        }
        return conexiones;
    }


    private void sendMessage(String[] datagrama) {
            System.out.println("LLEGO EL MENSAJE");
        DataOutputStream bufferDeSalida = null;
        for (Nodo nodo : poolSocket) {
            if (datagrama[2].equals(nodo.getName())) {
                try {
                    System.out.println("proceso de envio de mensaje");
                    bufferDeSalida = new DataOutputStream((nodo.getSocket().getOutputStream()));
                    bufferDeSalida.flush();
                    String mensaje = datagrama[3];
                    datagrama[3] = datagrama[1] + ": " + mensaje;
                    bufferDeSalida.writeUTF(datagrama[0] + ":" + datagrama[1] + ":" + datagrama[2] + ":" + datagrama[3]);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void sendImage(String[] datagrama) {
        System.out.println("LLEGO LA IMAGEN");
        DataOutputStream bufferDeSalida = null;
        for (Nodo nodo : poolSocket) {
            //historial(datagrama);
            if (datagrama[2].equals(nodo.getName())) {
                try {
                    bufferDeSalida = new DataOutputStream((nodo.getSocket().getOutputStream()));
                    bufferDeSalida.flush();
                    String mensaje = datagrama[3];
                    datagrama[3] = ("imagen" + ":" + mensaje);
                    bufferDeSalida.writeUTF(datagrama[0] + ":" + datagrama[1] + ":" + datagrama[2] + ":" + datagrama[3] + ":" + datagrama[4] + ":" + datagrama[5] + ":" + datagrama[6]);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }




}

