package edu.ubbcluj.ab2.minidb;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.json.JSONObject;

import java.io.*;
import java.net.*;

// TODO: ha leallitjuk a klienst akkor a server tovabb fusson es varja az uzeneteket

public class MyServer {
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    private final String fileName = "./data.json";
    private CatalogHandler catalogHandler;
    private Socket socket;
    private ServerSocket serverSocket;
    private Boolean run;
    private MongoClient mongoClient;

    public MyServer() {
        startConnection();
        mongoClient = MongoClients.create(new ConnectionString("mongodb://localhost:27017"));
        catalogHandler = isFileEmpty(fileName) ? new CatalogHandler(mongoClient) : new CatalogHandler(fileName, mongoClient);

        while (run) {
            handleMessage();
            catalogHandler.refreshContent(fileName);
        }
    }


    public void handleMessage() {
        String query = null;
        try {
            query = ((String) objectInputStream.readObject())
                    .replaceFirst("\\(", "")
                    .replaceAll("\\(", " ")
                    .replaceAll("\\)", " ")
                    .replaceAll(";", "")
                    .replaceAll(",", " ")
                    .replaceAll("\n", " ")
                    .replaceAll("\t", " ")
                    .replaceAll(" +", " ")
                    .replaceAll(" = ", " ")
                    .replaceAll("  ", " ");
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
            case "CREATE" -> {
                switch (message[1]) {
                    case "DATABASE" -> {
                        String databaseName = message[2];
                        createDatabase(databaseName);
                    }
                    case "TABLE" -> {
                        String[] string = message[2].split("\\.");
                        createTable(message, string[0], string[1]);
                    }
                    case "INDEX" -> {
                    }
                }
            }
            case "DROP" -> {
                switch (message[1]) {
                    case "DATABASE" -> {
                        String databaseName = message[2];
                        dropDatabase(databaseName);
                    }
                    case "TABLE" -> {
                        String[] string = message[2].split("\\.");
                        dropTable(string[0], string[1]);
                    }
                    //TODO: (OPTIONAL) delete attributes, pks, fks
                }
            }
            // if the client requests the name of the existent databases in the JSON file
            case "GETDATABASES" -> {
                writeIntoSocket(catalogHandler.getStringOfDatabases());
                System.out.println(catalogHandler.getStringOfDatabases());
            }
            // if the client requests the name of the existent tables in a database(message[1]) in the JSON file
            case "GETTABLES" -> {
                String databaseName = message[1];
                writeIntoSocket(catalogHandler.getStringOfTables(databaseName));
                System.out.println(catalogHandler.getStringOfTables(databaseName));
            }
            case "INSERT" -> {
                String[] string = message[2].split("\\.");
                insert(string[0], string[1], message);
            }
            case "DELETE" -> {
                String[] string = message[2].split("\\.");
                delete(string[0], string[1], message[5]);
            }
            default -> System.out.println("ERROR at reading Client's message!");
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


    public void writeIntoSocket(Object message) {
        try {
            objectOutputStream.writeObject(message);
        } catch (IOException e) {
            System.out.println("ERROR at writing object to socket!");
            endConnection();
        }
    }

    // Saves a JSONObject to a file
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

    public void createDatabase(String databaseName) {
        catalogHandler.createDatabase(databaseName);
    }

    public void dropDatabase(String databaseName) {
        catalogHandler.dropDatabase(databaseName);
    }

    public void createTable(String[] message, String databaseName, String tableName) {
        catalogHandler.createTable(databaseName, tableName);

        int i = 3;
        while (i < message.length) {
            String attrName = message[i++];
            String attrType = message[i++];
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
    }

    public void dropTable(String databaseName, String tableName) {
        catalogHandler.dropTable(databaseName, tableName);
    }

    public void insert(String databaseName, String tableName, String[] message) {
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection(tableName);
        Document document;
        int i = 4;
        //int nr = getNumberOfTableAttributes(tableName);
        int nr = 3;
        while (i < message.length) {
            document = new Document("key", message[i]);
            String value = message[i + 1];
            for (int j = i + 2; j < i + nr; j++) {
                value += "#" + message[j];
            }
            document.append("value", value);
            collection.insertOne(document);
            i += nr;
        }
    }

    public void delete(String databaseName, String tableName, String condition) {
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        MongoCollection<Document> table = database.getCollection(tableName);
        Document doc = table.find(Filters.eq("key", condition)).first();
        if (doc != null) {
            table.deleteOne(doc);
        } else {
            //hiba
        }
    }

    public static void main(String[] args) {
        new MyServer();
    }
}
