import org.json.simple.JSONObject;

import java.io.*;
import java.net.*;

public class MyServer {
    ObjectInputStream objectInputStream;
    ObjectOutputStream objectOutputStream;
    String fileName = "./data.json";
    private Socket socket;
    private ServerSocket serverSocket;
    private Boolean run;
    private Root databases;
    private Root.Database database;
    private Root.Database.Table table;
    private Root.Database.Table.Attribute attribute;
    private Root.Database.Table.PrimaryKey primaryKey;
    private Root.Database.Table.ForeignKey foreignKey;
    private Root.Database.Table.UniqueKey uniqueKey;
    private Root.Database.Table.ForeignKey.Reference reference;


    public MyServer() {
        startConnection();
        databases = new Root();
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
                .replaceFirst("\\(", "")
                .replaceAll("\\(", " ")
                .replaceAll("\\)", " ")
                .replaceAll(";", "")
                .replaceAll(",", "")
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
                        String databaseName = message[2];
                        database = databases.new Database(databases, databaseName);
                        break;
                    case "TABLE":
                        table = database.new Table(database, message[2]);
                        int i = 3;
                        while (i < message.length) {
                            String columnName = message[i++];
                            String columnType = message[i++];
                            //System.out.println(columnName + "       " + columnType);
                            switch (message[i]) {
                                case "FOREIGN": // Current column is Foreign Key
                                    i += 2;     // "KEY", "REFERENCES"
                                    String fkToTableName = message[i++];    // table
                                    String fkToColumnName = message[i++];   // column
                                    foreignKey = table.new ForeignKey(table, columnName);
                                    reference = foreignKey.new Reference(foreignKey, fkToTableName, fkToColumnName);
                                    break;
                                case "PRIMARY": // Current column is the Primary Key for the table
                                    i += 1;     // KEY
                                    primaryKey = table.new PrimaryKey(table, columnName, columnType);
                                    break;
                                default:
                                    break;
                            }

                            i += 1;
                            attribute = table.new Attribute(table, columnName, columnType);
                        }
                        break;
                    case "INDEX":
                        break;
                    default:
                        System.out.println("ERROR at reading Client's message!");
                        break;
                }
                saveToFile(databases.toJsonObject(), fileName);
                break;
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
                break;
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

    //Saves a JSONObjoect to a file
    public void saveToFile(JSONObject jsonObject, String fileName) {
        try {
            FileWriter file = new FileWriter(fileName);
            file.append(jsonObject.toJSONString());
            file.close();
        } catch (IOException e) {
            System.out.println("ERROR at writing to " + fileName + " file!");
            throw new RuntimeException(e);
        }
    }
}