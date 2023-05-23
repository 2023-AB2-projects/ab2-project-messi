package edu.ubbcluj.ab2.minidb;

import com.mongodb.ConnectionString;
import com.mongodb.MongoCommandException;
import com.mongodb.MongoException;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.connection.Stream;
import org.json.JSONObject;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import com.mongodb.client.MongoCollection;
import org.bson.Document;

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
                        ArrayList<String> fields = new ArrayList<>(Arrays.asList(message).subList(5, message.length));
//                        String field = message[5];
                        createIndex(String.join("#", fields), string[0], string[1], message[2]);
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
                    writeIntoSocket(catalogHandler.getStringOfAttributes(message[1], message[2]));

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

    // primary keys are in the first place guaranteed in attributes no matter the order they are in message
    public void createTable(String[] message, String databaseName, String tableName) {
        catalogHandler.createTable(databaseName, tableName);
        try {
            MongoDatabase database = mongoClient.getDatabase(databaseName);
            MongoCollection<Document> table = database.getCollection(tableName);
        } catch (Exception e) {
            System.out.println("An error occurred while creating the " + databaseName + "." + tableName + " table in MongoDB\n");
            e.printStackTrace();
        }

        ArrayList<String[]> attributesArray = new ArrayList<>();
        ArrayList<String[]> primaryKeysArray = new ArrayList<>();
        ArrayList<String[]> foreignKeysArray = new ArrayList<>();
        ArrayList<String[]> uniqueKeyArray = new ArrayList<>();

        int i = 3;
        while (i < message.length && !message[i].equals("CONSTRAINT")) {
            String attrName = message[i++];
            String attrType = message[i++];
            if (i >= message.length) {
                break;
            }
            String[] elements = {attrName, attrType};
            attributesArray.add(elements);
        }

        try {
            while (i < message.length) {
                i += 1; // CONSTRAINTS
                String indexName = message[i++];
                switch (message[i]) {
                    case "FOREIGN" -> { // Current column is Foreign Key
                        i += 2; // FOREIGN KEY
                        String attrName = message[i++];
                        i += 1; // REFERENCES
                        String refTableName = message[i++];
                        String refAttrName = message[i++];
                        String[] elements = {attrName, refTableName, refAttrName, indexName};
                        foreignKeysArray.add(elements);
                    }
                    case "PRIMARY" -> { // Current column is the Primary Key for the table
                        i += 2; // PRIMARY KEY
                        while (i < message.length && !message[i].equals("CONSTRAINT")) {
                            String pkName = message[i++];
                            String[] elements = {pkName};
                            primaryKeysArray.add(elements);
                        }
                    }
                    case "UNIQUE" -> { // Current column is Unique
                        i += 1;
                        while (i < message.length && !message[i].equals("CONSTRAINT")) {
                            String attrName = message[i++];
                            String[] elements = {attrName};
                            uniqueKeyArray.add(elements);
                        }
                    }
                }
            }
            primaryKeysArray.forEach(primaryKey -> catalogHandler.createPrimaryKey(databaseName, tableName, primaryKey[0]));
            uniqueKeyArray.forEach(uniqueKey -> {
                catalogHandler.createUnique(databaseName, tableName, uniqueKey[0]);
                createIndex(uniqueKey[0], databaseName, tableName, "UK");
            });

            // elsonek a primaryKey attributumokat rakjuk be az Attributes-ba
            attributesArray.removeIf(attribute -> {
                boolean isPrimaryKey = primaryKeysArray.stream().anyMatch(primaryKey -> Objects.equals(attribute[0], primaryKey[0]));
                if (isPrimaryKey) {
                    catalogHandler.createAttribute(databaseName, tableName, attribute[0], attribute[1]);
                }
                return isPrimaryKey;
            });

            // utana az uniqueKey attributumokat rakjuk be az Attributes-ba
            attributesArray.removeIf(attribute -> {
                boolean isUniqueKey = uniqueKeyArray.stream().anyMatch(uniqueKey -> Objects.equals(attribute[0], uniqueKey[0]));
                if (isUniqueKey) {
                    catalogHandler.createAttribute(databaseName, tableName, attribute[0], attribute[1]);
                }
                return isUniqueKey;
            });

            // utana minden mas attributumot
            attributesArray.forEach(attribute -> catalogHandler.createAttribute(databaseName, tableName, attribute[0], attribute[1]));

            foreignKeysArray.forEach(foreignKey -> {
                // foreignKey[0] = attrName; [1] = refTableName; [2] = refAttrName; [3] = indexName;
                catalogHandler.createForeignKey(databaseName, tableName, foreignKey[0]);
                catalogHandler.createReference(databaseName, tableName, foreignKey[0], foreignKey[1], foreignKey[2]);
                createIndex(foreignKey[0], databaseName, tableName, "FK");
            });
        } catch (Exception e) {
            e.printStackTrace();
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

    // primaryKey into indexValue vegere (insert)
    // value - table values
    //
    public void updateIndex(String primaryKey, String value, String databaseName, String tableName, String operation) {
        try {
            MongoDatabase database = mongoClient.getDatabase(databaseName);
            MongoCollection<Document> table = database.getCollection(tableName);

            String indexNames = catalogHandler.getIndexNames(databaseName);
            Arrays.stream(indexNames.split(" ")).forEach(indexName -> {
                if (indexName.contains(tableName)) {
                    String fields = catalogHandler.getCertainIndexFields(databaseName, indexName);
                    if (Objects.equals(fields, "")) {
                        return;
                    }
                    int[] indexOfFields = Arrays.stream(fields.split("#"))
                            .mapToInt(field -> (catalogHandler.getIndexOfAttribute(databaseName, tableName, field) - catalogHandler.getNumberOfPrimaryKeys(databaseName, tableName)))
                            .toArray();

                    MongoCollection<Document> indexTable = database.getCollection(indexName);
                    String[] values = value.split("#");
                    String[] id = Arrays.stream(indexOfFields)
                            .mapToObj(index -> values[index])
                            .toArray(String[]::new);
                    String joinedId = String.join("#", id);

                    Document document = indexTable.find(Filters.eq("_id", joinedId)).first();
                    String newValue = "";
                    if (operation.equals("insert")) {
                        if (document != null) {
                            String existingValue = document.getString("value");
                            if (!existingValue.contains(primaryKey)) {
                                newValue = existingValue.isEmpty() ? primaryKey : existingValue + "#" + primaryKey;
                            }
                        } else {
                            indexTable.insertOne(new Document().append("_id", joinedId).append("value", primaryKey));
                            return;
                        }
                    } else if (operation.equals("delete")) {
                        if (document != null) {
                            String existingValue = document.getString("value");
                            newValue = existingValue.replace(primaryKey, "").replaceAll("##", "#");
                        }
                    }
                    if (!newValue.equals("")) {
                        indexTable.findOneAndUpdate(Filters.eq("_id", joinedId), Updates.set("value", newValue));
                    } else {
                        indexTable.findOneAndDelete(Filters.eq("_id", joinedId));
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createIndex(String fields, String databaseName, String tableName, String indexName) {
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        MongoCollection<Document> collection = database.getCollection(tableName);
        indexName = indexName.concat("_").concat(databaseName).concat("_").concat(tableName).concat("_").concat(fields);

        if (catalogHandler.existsIndex(databaseName, indexName)) {
            System.out.println("An error occurred while creating the index. The index already exist.\n");
            return;
        }
        catalogHandler.createIndex(databaseName, indexName, fields);

        System.out.println("\nindexName: " + indexName + "\n");
        database.createCollection(indexName);
        MongoCollection<Document> indexTableName = database.getCollection(indexName);

        int[] indexOfFields = Arrays.stream(fields.split("#"))
                .mapToInt(field -> (catalogHandler.getIndexOfAttribute(databaseName, tableName, field) - catalogHandler.getNumberOfPrimaryKeys(databaseName, tableName)))
                .toArray();
        for (int indexOfField : indexOfFields) {
            if (indexOfField == -1) {
                System.out.println("An error occurred while finding the index of the " + indexOfField + " attribute in " + fileName + "\n");
                return;
            }
        }

        HashMap<String, ArrayList<String>> hashMap = new HashMap<>();
        for (Document document : database.getCollection(tableName).find()) {
            String[] values = document.getString("value").split("#");
            String[] keyNames = Arrays.stream(indexOfFields)
                    .mapToObj(index -> values[index])
                    .toArray(String[]::new);
            String joinedKey = String.join("#", keyNames);
            ArrayList<String> indexValue = (hashMap.get(joinedKey) == null) ? new ArrayList<>() : hashMap.get(joinedKey);
            indexValue.add(document.getString("_id"));
            hashMap.put(joinedKey, indexValue);
        }

        hashMap.forEach((key, value) -> indexTableName.insertOne(new Document().append("_id", key).append("value", value.stream().map(Object::toString).collect(Collectors.joining("#")))));
    }

    private AtomicBoolean existsIndex(String id, String databaseName, String tableName) {
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        MongoCollection<Document> table = database.getCollection(tableName);

        AtomicBoolean foundIndex = new AtomicBoolean(false);

        String indexNames = catalogHandler.getIndexNames(databaseName);
        Arrays.stream(indexNames.split(" ")).forEach(indexName -> {
            if (indexName.contains(tableName)) {
                MongoCollection<Document> indexTable = database.getCollection(indexName);
                Document document = indexTable.find(Filters.eq("_id", id)).first();
                if (document != null) {
                    foundIndex.set(true);
                }
            }
        });
        return foundIndex;
    }


    public void insert(String databaseName, String tableName, String[] message) {
        try {
            MongoDatabase database = mongoClient.getDatabase(databaseName);
            MongoCollection<Document> collection = database.getCollection(tableName);

            int nr = catalogHandler.getNumberOfAttributes(databaseName, tableName);
            if (nr != message.length - 4) {
                System.out.println("An error occurred while inserting the " + tableName + " table in " + databaseName + " database.\n");
                return;
            }

            int nrPK = catalogHandler.getNumberOfPrimaryKeys(databaseName, tableName);
            int i = 4;
            nr -= nrPK;

            String id = "";
            String value = "";
            while (i < message.length) {
                id += message[i];
                for (int j = 1; j < nrPK; j++) {
                    id += "#" + message[i + j];
                }
                i += nrPK;

                if (i < message.length) {
                    value += message[i];
                    for (int j = 1; j < nr; j++) {
                        value += "#" + message[i + j];
                    }
                    i += nr;

                    int nrOfUniqueKeys = catalogHandler.getNumberOfUniqueKeys(databaseName, tableName);
                    if (nrOfUniqueKeys != 0) {
                        String unique = "";
                        for (int k = 0; k < nrOfUniqueKeys; k++) {
                            unique += value.split("#")[k];
                        }
                        unique = String.join("#",unique);
                        System.out.println(unique);
                        AtomicBoolean atomicBoolean = existsIndex(unique, databaseName, tableName);
                        if (atomicBoolean.get()) {
                            System.out.println("Unique key(s): " + unique + " already defined\n");
                            return;
                        }
                    }
                    if (collection.find(new Document("_id", id)).first() != null) {
                        System.out.println("The document with id: " + id + " already exists in " + databaseName + "." + tableName + " table\n");
                        return;
                    }
                    collection.insertOne(new Document("_id", id).append("value", value));
                    System.out.println("Successfully inserted into " + databaseName + "." + tableName + " with id: " + id + " values: " + value);
                }
            }
            updateIndex(id, value, databaseName, tableName, "insert");
        } catch (Exception e) {
            System.out.println("An error occurred while inserting into the " + databaseName + "." + tableName + " table in MongoDB\n");
            e.printStackTrace();
        }
    }

    public void delete(String databaseName, String tableName, String id) {
        try {
            MongoDatabase database = mongoClient.getDatabase(databaseName);
            MongoCollection<Document> table = database.getCollection(tableName);
            Document document = table.find(Filters.eq("_id", id)).first();
            if (document != null) {
                AtomicBoolean atomicBoolean = existsIndex(id, databaseName, tableName);
                if (atomicBoolean.get()) {
                    System.out.println("The document with id: " + id + " can not be deleted because it is referenced by a foreign key constraint in another table.");
                } else {
                    System.out.println("Successfully deleted the document with id: " + id + " from the " + databaseName + "." + tableName + " table.");
                    table.deleteOne(document);
                }
            } else {
                System.out.println("The document with id: " + id + " does not exists in " + databaseName + "." + tableName + " table\n");
            }
        } catch (Exception e) {
            System.out.println("An error occured while deleting from " + databaseName + "." + tableName + " table in MongoDB\n");
            e.printStackTrace();
        }
    }
}