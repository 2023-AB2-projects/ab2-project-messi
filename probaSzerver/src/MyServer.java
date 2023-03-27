import java.io.*;
import java.net.*;

public class MyServer {
    ObjectInputStream objectInputStream;
    ObjectOutputStream objectOutputStream;
    private Socket socket;
    private ServerSocket serverSocket;
    private Boolean run;

    public MyServer() {
        startConnection();
        while (run) {
            try {
                handleMessage();
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("ERROR at reading object from socket!");
            }
        }
    }

    public static void main(String[] args) {
        new MyServer();
    }

    public void handleMessage() throws IOException, ClassNotFoundException {
        String[] message = ((String) objectInputStream.readObject()).split("/");
        for (String i : message) {
            System.out.println(i + " ");
        }
        switch (message[0]) {
            //create database:
            case "1":

                break;
            //drop database:
            case "2":
                break;
            //create table:
            case "3":

                break;
            //drop table:
            case "4":
                break;
            //create index:
            case "5":
                break;
            // ...
            case "6":
                break;
            default:
                System.out.println("ERROR message content");
                break;
        }
    }

    private void startConnection() {
        try {
            serverSocket = new ServerSocket(1111);
            socket = serverSocket.accept();
            objectInputStream = new ObjectInputStream(socket.getInputStream());
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            run = true;
        } catch (IOException e) {
            System.out.println("ERROR at initializing connection!");
            endConnection();
        }
    }

    private void endConnection() {
        try {
            //close sockets
            serverSocket.close();
            socket.close();
            //close streams
            objectOutputStream.close();
            objectInputStream.close();
        } catch (IOException e) {
            System.out.println("ERROR at ending connection!");
        }
    }
}
