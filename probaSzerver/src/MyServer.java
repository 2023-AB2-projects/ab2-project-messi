import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.net.*;
import java.util.Arrays;
import java.util.Objects;

public class MyServer {
    ObjectInputStream objectInputStream;
    ObjectOutputStream objectOutputStream;
    private Socket socket;
    private ServerSocket serverSocket;
    private Boolean run;
    private Database database;
    private Database.Table tables;

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
        String query = ((String) objectInputStream.readObject())
                .replaceAll("\\(", "")
                .replaceAll("\\)", "")
                .replaceAll(";", "")
                .replaceAll("\n", " ")
                .replaceAll(" +", " ");

        String[] message = query.split(" ");

        for (String i : message) {
            System.out.println(i + " ");
        }
        switch (message[0]) {
            //create database:
            case "CREATE":
                switch (message[1]) {
                    case "DATABASE":
                        database = new Database(message[2]);
                        break;
                    case "TABLE":
                        tables = database.new Table(database, message[2]);
                        int i = 3;
                        while (i < message.length) {
                            if (Objects.equals(message[i], "true")) {    //Current column is Primary Key for the table
                                System.out.println("PRIMARY KEY: " + message[i] + "     " + message[i + 1]);
                                i++;
                                Database.Table.PrimaryKey primaryKey = tables.new PrimaryKey(tables, message[i], message[i + 1]);
                            } else {
                                i++;
                            }

                            Database.Table.Attribute attribute = tables.new Attribute(tables, message[i], message[i + 1]);
                            System.out.println(message[i] + "     " + message[i + 1]);
                            i += 2;
                        }

                        JSONObject jsonObject = database.toJsonObject();
                        FileWriter file = new FileWriter("output.json");
                        file.write(jsonObject.toJSONString());
                        file.close();
                        break;
                    case "INDEX":
                        break;
                    default:
                        System.out.println("ERROR at reading Client's message!");
                        break;
                }
            case "DROP":
                switch (message[1]) {
                    case "DATABASE":
                        break;
                    case "TABLE":
                        break;
                    default:
                        System.out.println("ERROR at reading Client's message!");
                        break;
                }

            default:
                System.out.println("ERROR at reading Client's message!");
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
