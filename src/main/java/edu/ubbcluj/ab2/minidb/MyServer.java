package edu.ubbcluj.ab2.minidb;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;


// TODO: ha leallitjuk a klienst akkor a server tovabb fusson es varja az uzeneteket


import java.io.*;
import java.net.*;

public class MyServer {
    ObjectInputStream objectInputStream;
    ObjectOutputStream objectOutputStream;
    String fileName = "data.json";
    CatalogHandler catalogHandler;
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
    private String databaseName;
    private String tableName;
    private String attrName;
    private String attrType;

//    private Logger logger;


    public MyServer() {
//        logger = LoggerFactory.getLogger(MyServer.class);
        startConnection();

        catalogHandler = isFileEmpty(fileName) ? new CatalogHandler() : new CatalogHandler(fileName);

        while (run) {
            handleMessage();
        }
    }

    public static void main(String[] args) {
        new MyServer();
    }

    public void handleMessage() {
        String query = null;
        try {
            query = ((String) objectInputStream.readObject())
                    .replaceFirst("\\(", "")
                    .replaceAll("\\(", " ")
                    .replaceAll("\\)", " ")
                    .replaceAll(";", "")
                    .replaceAll(",", "")
                    .replaceAll("\n", " ")
                    .replaceAll(" +", " ");
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("ERROR at reading object from socket!");
            endConnection();
        }

        assert query != null;
        String[] message = query.split(" ");

        System.out.print("\nmessage: ");
        for (String i : message) {
            System.out.print(i + " ");
        }
        switch (message[0]) {
            // create database:
            case "CREATE":
                switch (message[1]) {
                    case "DATABASE":
                        databaseName = message[2];
                        catalogHandler.createDatabase(databaseName);
                        break;
                    case "TABLE":
                        databaseName = message[2].split("\\.")[0];
                        tableName = message[2].split("\\.")[1];
                        catalogHandler.createTable(databaseName, tableName);
                        int i = 3;
                        while (i < message.length) {
                            attrName = message[i++];
                            attrType = message[i++];
                            if (i >= message.length) {
                                break;
                            }
                            switch (message[i]) {
                                case "FOREIGN" -> { // Current column is Foreign Key
                                    i += 3;     // "KEY", "REFERENCES"
                                    String refTableName = message[i++];
                                    String refAttrName = message[i++];
                                    catalogHandler.createForeignKey(databaseName, tableName, attrName);
                                    catalogHandler.createReference(databaseName, tableName, attrName, refTableName, refAttrName);
                                }
                                case "PRIMARY" -> { // Current column is the Primary Key for the table
                                    i += 2;     // KEY
                                    catalogHandler.createPrimaryKey(databaseName, tableName, attrName, attrType);
                                }
                            }
                            catalogHandler.createAttribute(databaseName, tableName, attrName, attrType);
                        }
                        break;
                    case "INDEX":
                        break;
                    default:
                        System.out.println("ERROR at reading Client's message!");
                        break;
                }
                break;
            case "DROP":
                switch (message[1]) {
                    case "DATABASE" -> {
                        databaseName = message[2];
                        catalogHandler.deleteDatabase(databaseName);
                    }
                    case "TABLE" -> {
                        String[] string = message[2].split("\\.");
                        databaseName = string[0];
                        tableName = string[1];
                        catalogHandler.deleteTable(databaseName, tableName);
                    }

                    //TODO: (OPTIONAL) delete attributes, pks, fks
                    default -> System.out.println("ERROR at reading Client's message!");
                }
                break;
            // if the client requests the name of the existent databases in the JSON file
            case "GETDATABASES":
                writeIntoSocket(catalogHandler.getStringOfDatabases());
                System.out.println(catalogHandler.getStringOfDatabases());
                break;
            case "GETTABLES":
                databaseName = message[1];
                writeIntoSocket(catalogHandler.getStringOfTables(databaseName));
                System.out.println(catalogHandler.getStringOfTables(databaseName));
                break;
            default:
                System.out.println("ERROR at reading Client's message!");
                break;
        }
        catalogHandler.saveCatalogToFile(fileName);
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

    public void writeIntoSocket(String message) {
        try {
            objectOutputStream.writeObject(message);
        } catch (IOException e) {
            System.out.println("ERROR at writing object to socket!");
            endConnection();
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

    public boolean isFileEmpty(String filePath) {
        File file = new File(filePath);
        return (file.length() == 0);
    }
}
