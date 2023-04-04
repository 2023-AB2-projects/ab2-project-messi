package edu.ubbcluj.ab2.minidb;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;

// TODO: ha leallitjuk a klienst akkor a server tovabb fusson es varja az uzeneteket


import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MyServer {
    ObjectInputStream objectInputStream;
    ObjectOutputStream objectOutputStream;
    String fileName = "data.json";
    private Socket socket;
    private ServerSocket serverSocket;
    private Boolean run;
    private ObjectMapper objectMapper;
    private JsonNode jsonNode;
    private Root root;
    private Root.Database database;
    private Root.Database.Table table;
    private Root.Database.Table.Attribute attribute;
    private Root.Database.Table.PrimaryKey primaryKey;
    private Root.Database.Table.ForeignKey foreignKey;
    private Root.Database.Table.UniqueKey uniqueKey;
    private Root.Database.Table.ForeignKey.Reference reference;


    public MyServer() {
        startConnection();
        //System.out.println("\n-----\n-----\n-----\n-----\n-----\n-----\n-----\n-----\n-----\n-----\n-----\n-----");

        /*objectMapper = new ObjectMapper();
        try {
            jsonNode = objectMapper.readTree("{\"Databases\":[{\"Database\":\"alma\",\"Tables\":[]}]}");
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }*/

        /*try {
            objectMapper = new ObjectMapper();
            URL resource = getClass().getClassLoader().getResource("data.json");
            File file = new File(resource.toURI());
            String json = String.valueOf(Files.readAllBytes(Paths.get(file.toURI())));
            // InputStream is = getClass().getClassLoader().getResourceAsStream("data.json");
            jsonNode = objectMapper.readTree("{\"Databases\":[{\"Database\":\"alma\",\"Tables\":[]}]}");
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }*/

        while (run) {
            handleMessage();
        }
    }

    public void handleMessage(){
        String query = null;
        try {
            query = ((String) objectInputStream.readObject()).replaceFirst("\\(", "").replaceAll("\\(", " ").replaceAll("\\)", " ").replaceAll(";", "").replaceAll(",", "").replaceAll("\n", " ").replaceAll(" +", " ");
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Can't read object from socket!");
        }

        String[] message = query.split(" ");

        for (String i : message) {
            System.out.println(i + " ");
        }
        switch (message[0]) {
            // create database:
            case "CREATE":
                switch (message[1]) {
                    case "DATABASE":
                        String databaseName = message[2];
                        Root.Database database = root.new Database(root, databaseName);
                        break;
                    case "TABLE":
                        //Root.Database.Table table = database.new Table(database, message[2]);
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
                                    Root.Database.Table.ForeignKey foreignKey = table.new ForeignKey(table, columnName);
                                    foreignKey.new Reference(foreignKey, fkToTableName, fkToColumnName);
                                    break;
                                case "PRIMARY": // Current column is the Primary Key for the table
                                    i += 1;     // KEY
                                    Root.Database.Table.PrimaryKey primaryKey = table.new PrimaryKey(table, columnName, columnType);
                                    break;
                                default:
                                    break;
                            }

                            i += 1;
                            Root.Database.Table.Attribute attribute = table.new Attribute(table, columnName, columnType);
                        }
                        break;
                    case "INDEX":
                        break;
                    default:
                        System.out.println("ERROR at reading Client's message!");
                        break;
                }
                saveToFile(root.toJsonObject(), fileName);
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
            // if the client requests the name of the existent databases in the JSON file
            case "GETDATABASES":
                /*JsonNode databases = jsonNode.get("Database");
                databases.forEach(db -> {
                    System.out.println(db.get("Database"));
                });*/
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
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectInputStream = new ObjectInputStream(socket.getInputStream());
            run = true;
        } catch (IOException e) {
            System.out.println("ERROR at initializing connection!");
            endConnection();
        }
    }

    private void endConnection() {
        try {
            // close streams
            objectInputStream.close();
            objectOutputStream.close();
            // close sockets
            socket.close();
            serverSocket.close();
            run = false;
        } catch (IOException e) {
            System.out.println("ERROR at ending connection!");
        }
    }

    //Saves a JSONObjoect to a file
    public void saveToFile(JSONObject jsonObject, String fileName) {
        try {
            FileWriter file = new FileWriter(fileName);
            file.append(jsonObject.toString());
            file.close();
        } catch (IOException e) {
            System.out.println("ERROR at writing to " + fileName + " file!");
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        System.out.println("aa");
        new MyServer();
    }
}
