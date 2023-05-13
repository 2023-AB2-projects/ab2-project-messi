package edu.ubbcluj.ab2.minidb;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import org.bson.Document;
import org.json.JSONObject;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.mongodb.client.model.Indexes.ascending;
import static com.mongodb.client.model.Indexes.descending;

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
        mongoClient = MongoClients.create(new ConnectionString("mongodb://localhost:27017/"));
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
                    case "UNIQUE" -> {      // CREATE UNIQUE INDEX

                        String[] string = message[5].split("\\.");
                        ArrayList<String> fields = new ArrayList<>(Arrays.asList(message).subList(6 , message.length));
                        System.out.println(fields);
                        createIndex(fields, string[0], string[1], message[3], true);
                    }
                    case "INDEX" -> {       // CREATE INDEX
                        String[] string = message[4].split("\\.");
                        ArrayList<String> fields = new ArrayList<>(Arrays.asList(message).subList(5 , message.length));
                        createIndex(fields, string[0], string[1], message[2], false);
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
        MongoDatabase database = mongoClient.getDatabase(databaseName);
    }

    public void dropDatabase(String databaseName) {
        catalogHandler.dropDatabase(databaseName);
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        database.drop();
    }

    public void createTable(String[] message, String databaseName, String tableName) {
        catalogHandler.createTable(databaseName, tableName);
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        MongoCollection<Document> table = database.getCollection(tableName);

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

    public void createIndex(ArrayList<String> fields, String databaseName, String tableName, String indexName, Boolean isUnique) {
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection(tableName);
        indexName = "Index_".concat(databaseName.concat("_")).concat(tableName.concat("_")).concat(indexName);
        List<Document> indexList = new ArrayList<>();
        for (int i = 0; i < fields.size(); i += 2) {
            String field = fields.get(i);
            String sortOrder = fields.get(i + 1);
            Document indexSpec;
            if (sortOrder.equals("ASC")) {
                indexSpec = (Document) Indexes.ascending(field);
            } else {
                indexSpec = (Document) Indexes.descending(field);
            }
            if (isUnique) {
                indexSpec.append("unique", true);
            }
            indexList.add(indexSpec);
        }
        System.out.println(indexList);
        Document[] indexArray =  indexList.toArray(new Document[0]);

        collection.createIndex(Indexes.compoundIndex(indexArray));
        System.out.println("\nIndex created\n");
    }

    public void dropTable(String databaseName, String tableName) {
        catalogHandler.dropTable(databaseName, tableName);
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection(tableName);
        collection.drop();
    }

    public void insert(String databaseName, String tableName, String[] message) {
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection(tableName);

        int nr = catalogHandler.getNumberOfAttributes(databaseName, tableName);
        if (nr != message.length - 4) {
            System.out.println("Did not complete insertion: Column name or number of supplied values does not match table definition");
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
        }
    }

    public void delete(String databaseName, String tableName, String condition) {
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        MongoCollection<Document> table = database.getCollection(tableName);
        Document doc = table.find(Filters.eq("_id", condition)).first();
        System.out.println(condition);
        if (doc != null) {
            table.deleteOne(doc);
        } else {
            System.out.println("There is no data with such ID. 0 rows deleted");
        }
    }

    public static void main(String[] args) {
        new MyServer();
    }
}
