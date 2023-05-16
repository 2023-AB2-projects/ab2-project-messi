package edu.ubbcluj.ab2.minidb;

import com.mongodb.ConnectionString;
import com.mongodb.MongoCommandException;
import com.mongodb.MongoException;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import org.json.JSONObject;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.mongodb.client.MongoCollection;
import org.bson.Document;

import javax.print.Doc;

// TODO: ha leallitjuk a klienst akkor a server tovabb fusson es varja az uzeneteket

public class MyServer {
    private final String fileName = "./data.json";
    private ObjectInputStream objectInputStream;
    private ObjectOutputStream objectOutputStream;
    private CatalogHandler catalogHandler;
    private Socket socket;
    private ServerSocket serverSocket;
    private Boolean run;
    private MongoClient mongoClient;

    public MyServer() {
        startConnection();
        try {
            mongoClient = MongoClients.create(new ConnectionString("mongodb://localhost:27017/"));
            System.out.println("Connection with mongoDB was established");
        } catch (MongoException e) {
            System.out.println("An error occurred while establishing connection with mongoDB\n");
            e.printStackTrace();
        }

        catalogHandler = isFileEmpty(fileName) ? new CatalogHandler(mongoClient) : new CatalogHandler(fileName, mongoClient);

        while (run) {
            handleMessage();
            catalogHandler.refreshContent(fileName);
        }
    }

    public static void main(String[] args) {
        new MyServer();
    }

    public void handleMessage() {
        String query = null;
        try {
            query = ((String) objectInputStream.readObject()).replaceAll("\\(", " ").replaceAll("\\)", " ").replaceAll(";", "").replaceAll(",", " ").replaceAll("\n", " ").replaceAll("\t", " ").replaceAll(" +", " ").replaceAll(" = ", " ").replaceAll("  ", " ");
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("An error occurred while reading object from socket!\n");
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
                    case "INDEX" -> {       // CREATE INDEX
                        String[] string = message[4].split("\\.");
//                        ArrayList<String> fields = new ArrayList<>(Arrays.asList(message).subList(5, message.length));
                        // TODO: megcsinalni tobb fields-re
                        String field = message[5];
                        createIndex(field, string[0], string[1], message[2]);
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
            case "GETDATABASES" -> writeIntoSocket(catalogHandler.getStringOfDatabases());
            // if the client requests the name of the existent tables in a database(message[1]) in the JSON file
            case "GETTABLES" -> {
                if (message.length == 2) {
                    writeIntoSocket(catalogHandler.getStringOfTables(message[1]));

                } else {
                    writeIntoSocket("");
                }
            }
            // if the client requests the nam of the fields of one table from a specified database
            case "GETFIELDS" -> {
                if (message.length == 3) {
                    writeIntoSocket(catalogHandler.getStringOfTableFields(message[1], message[2]));

                } else {
                    writeIntoSocket("");
                }
            }
            case "INSERT" -> {
                String[] string = message[2].split("\\.");
                insert(string[0], string[1], message);
            }
            case "DELETE" -> {
                String[] string = message[2].split("\\.");
                delete(string[0], string[1], message[5]);
            }
            default -> System.out.println("An error occurred while reading Client's message!\n");
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
            System.out.println("An error occurred while initializing connection!\n");
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
            System.out.println("An error occurred while ending connection!\n");
        }
    }

    public void writeIntoSocket(Object message) {
        try {
            objectOutputStream.writeObject(message);
        } catch (IOException e) {
            System.out.println("An error occurred while writing object to socket!\n");
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
            System.out.println("An error occurred while writing to " + fileName + " file!\n");
            throw new RuntimeException(e);
        }
    }

    public boolean isFileEmpty(String filePath) {
        File file = new File(filePath);
        return (file.length() == 0);
    }

    public void createDatabase(String databaseName) {
        catalogHandler.createDatabase(databaseName);
        try {
            MongoDatabase database = mongoClient.getDatabase(databaseName);
            System.out.println("Succesfully created the " + databaseName + " database in MongoDB\n");
        } catch (MongoException e) {
            System.out.println("An error occurred while creating a database in MongoDB:\n");
            e.printStackTrace();
        }
    }

    public void dropDatabase(String databaseName) {
        catalogHandler.dropDatabase(databaseName);
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        try {
            database.drop();
            System.out.println("Succesfully dropped the " + databaseName + " database in MongoDB\n");
        } catch (MongoCommandException e) {
            System.out.println("An error occurred while droping the " + databaseName + " database in MongoDB:\n");
            e.printStackTrace();
        }
    }

    public void createTable(String[] message, String databaseName, String tableName) {
        catalogHandler.createTable(databaseName, tableName);
        try {
            MongoDatabase database = mongoClient.getDatabase(databaseName);
            MongoCollection<Document> table = database.getCollection(tableName);
        } catch (Exception e) {
            System.out.println("An error occurred while creating the " + databaseName + "." + tableName + " table in MongoDB\n");
            e.printStackTrace();
        }

        int i = 3;
        while (i < message.length && !message[i].equals("CONSTRAINT")) {
            String attrName = message[i++];
            String attrType = message[i++];
            if (i >= message.length) {
                break;
            }
            catalogHandler.createAttribute(databaseName, tableName, attrName, attrType);
        }

        // CONSTRAINTS
        while (i < message.length) {
            i += 2;
            switch (message[i]) {
                case "FOREIGN" -> { // Current column is Foreign Key
                    String attrName = message[i + 2];
                    i += 3; // "REFERENCES"
                    String refTableName = message[i++];
                    String refAttrName = message[i++];
                    catalogHandler.createForeignKey(databaseName, tableName, attrName);
                    catalogHandler.createReference(databaseName, tableName, attrName, refTableName, refAttrName);
                }
                case "PRIMARY" -> { // Current column is the Primary Key for the table
                    i += 2;
                    while (i < message.length && !message[i].equals("CONSTRAINT")) {
                        catalogHandler.createPrimaryKey(databaseName, tableName, message[i]);
                        i++;
                    }
                }
            }
        }
    }

    public void dropTable(String databaseName, String tableName) {
        try {
            catalogHandler.dropTable(databaseName, tableName);
            MongoDatabase database = mongoClient.getDatabase(databaseName);
            MongoCollection<Document> collection = database.getCollection(tableName);
            collection.drop();
        } catch (Exception e) {
            System.out.println("An error occurred while dropping the " + databaseName + "." + tableName + " table in MongoDB\n");
        }
    }

    public void createIndex(String fieldName, String databaseName, String tableName, String indexName) {
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection(tableName);
        indexName = indexName.concat("_").concat(databaseName).concat("_").concat(tableName);

        if (catalogHandler.getInstanceOfTable(databaseName, indexName) != null) {
            System.out.println("An error occured while creating the index. The index already exist.\n");
            return;
        }

        // TODO: JSON file-ba is elmenteni ha szukseges az indexTable-t
        database.createCollection(indexName);
        MongoCollection<Document> indexTableName = database.getCollection(indexName);

        int nrOfPrimaryKey = catalogHandler.getNumberOfPrimaryKeys(databaseName, tableName);
        int indexOfField = catalogHandler.getIndexOfAttribute(databaseName, tableName, fieldName);
        int i = indexOfField - nrOfPrimaryKey;

        HashMap<String, ArrayList<String>> map = new HashMap<>();
        for (Document document : database.getCollection(tableName).find()) {
            String indexKey = document.getString("value").split("#")[i];
            ArrayList<String> indexValue = (map.get(indexKey) == null) ? indexValue = new ArrayList<>() : map.get(indexKey);
            indexValue.add(document.getString("_id"));
            System.out.println(indexKey);
            System.out.println(indexValue);
            map.put(indexKey, indexValue);
        }

        map.forEach((key, value) -> indexTableName.insertOne(new Document().append("_id", key).append("value", value.stream().map(Object::toString).collect(Collectors.joining("#")))));
        System.out.println("\nIndex created\n");
    }


    public void insert(String databaseName, String tableName, String[] message) {
        try {
            MongoDatabase database = mongoClient.getDatabase(databaseName);
            MongoCollection<Document> collection = database.getCollection(tableName);

            int nr = catalogHandler.getNumberOfAttributes(databaseName, tableName);
            if (nr != message.length - 4) {
                System.out.println("Did not complete insertion: Column name or number of supplied values does not match table definition\n");
                return;
            }

            String value = "", id;
            int i = 4, nrPK = catalogHandler.getNumberOfPrimaryKeys(databaseName, tableName);
            nr -= nrPK;
            while (i < message.length) {
                id = message[i];
                for (int j = 1; j < nrPK; j++) {
                    id += "#" + message[i + j];
                }
                i += nrPK;

                if (i < message.length) {
                    value += message[i];
                    for (int j = 1; j < nr; j++) {
                        value += "#" + (message[i + j]);
                    }
                    i += nr;
                }
                collection.insertOne(new Document().append("_id", id).append("value", value));
                System.out.println("Succesfully insertedd into " + databaseName + "." + tableName + " with id: " + id + " values: " + value);
            }
        } catch (Exception e) {
            System.out.println("An error occurred while inserting into the " + databaseName + "." + tableName + " table in MongoDB\n");
            e.printStackTrace();
        }
    }

    public void delete(String databaseName, String tableName, String condition) {
        try {
            MongoDatabase database = mongoClient.getDatabase(databaseName);
            MongoCollection<Document> table = database.getCollection(tableName);
            Document doc = table.find(Filters.eq("_id", condition)).first();
            System.out.println(condition);
            if (doc != null) {
                table.deleteOne(doc);
            } else {
                System.out.println("There is no data with such ID. 0 rows deleted\n");
            }
        } catch (Exception e) {
            System.out.println("An error occured while deleting from " + databaseName + "." + tableName + " table in MongoDB\n");
            e.printStackTrace();
        }
    }
}