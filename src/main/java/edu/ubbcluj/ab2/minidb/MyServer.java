package edu.ubbcluj.ab2.minidb;

import com.mongodb.ConnectionString;
import com.mongodb.MongoCommandException;
import com.mongodb.MongoException;
import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
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
    private ArrayList<String> selectFields;

    public MyServer() {
        startConnection();
        try {
            mongoClient = MongoClients.create(new ConnectionString("mongodb://localhost:27017/"));
        } catch (MongoException e) {
            System.out.println("\nAn error occurred while establishing connection with mongoDB\n");
            e.printStackTrace();
        }

        System.out.println("Connection with mongoDB was established");
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
            query = ((String) objectInputStream.readObject()).replaceAll("\\(", " ").replaceAll("\\)", " ").replaceAll(";", "").replaceAll(",", " ").replaceAll("\n", " ").replaceAll("'", " ").replaceAll("\t", " ").replaceAll(" +", " ").replaceAll("  ", " ");
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("\nAn error occurred while reading object from socket!\n");
            endConnection();
        }

        // assert query != null;
        if (query == null) {
            return;
        }
        String[] message = query.split(" ");
//        String[] message = "SELECT l.Id s.Id FROM School.Students s INNER JOIN Location l ON l.Id = s.FK_LocID WHERE l.Street = Paris".split(" ");
//        String[] message = "SELECT AVG FK_LocID FROM School.Students GROUP BY FK_LocID".split(" ");
//        String[] message = "SELECT s.Id s.Name l.StrSELECT * FROM School.Students s INNER JOIN Location l ON l.Id = s.FK_LocID WHERE Street = Paris joinTableseet t.Name FROM School.Location l INNER JOIN School.Student s ON l.Id = s.LocId".split(" ");
//        String[] message = "SELECT * FROM School.Students s INNER JOIN Location l ON l.Id = s.FK_LocID WHERE l.Street = Paris".split(" ");
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
            case "SELECT" -> select(message);
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
                delete(string[0], string[1], message[6]);
            }
            case "GETFIELDTYPE" -> {
                if (message.length == 4) {
                    if (catalogHandler.isValidAttributeType(message[1], message[2], message[3]) == 0) {
                        writeIntoSocket("NUMERIC");
                    } else {
                        writeIntoSocket("NOT NUMERIC");
                    }
                } else {
                    writeIntoSocket("");
                }
            }
            default -> System.out.println("\nAn error occurred while reading Client's message!\n");
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
            System.out.println("\nAn error occurred while initializing connection!\n");
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
            System.out.println("\nAn error occurred while ending connection!\n");
        }
    }

    public void writeIntoSocket(Object message) {
        try {
            objectOutputStream.writeObject(message);
        } catch (IOException e) {
            System.out.println("\nAn error occurred while writing object to socket!\n");
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
            System.out.println("\nAn error occurred while writing to " + fileName + " file!\n");
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
            System.out.println("\nSuccesfully created the " + databaseName + " database in MongoDB\n");
        } catch (MongoException e) {
            System.out.println("\nAn error occurred while creating a database in MongoDB:\n");
            e.printStackTrace();
        }
    }

    public void dropDatabase(String databaseName) {
        catalogHandler.dropDatabase(databaseName);
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        try {
            database.drop();
            System.out.println("\nSuccesfully dropped the " + databaseName + " database in MongoDB\n");
        } catch (MongoCommandException e) {
            System.out.println("\nAn error occurred while droping the " + databaseName + " database in MongoDB:\n");
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
            // TODO: drop indexes aswell
            catalogHandler.dropTable(databaseName, tableName);
            MongoDatabase database = mongoClient.getDatabase(databaseName);
            MongoCollection<Document> collection = database.getCollection(tableName);
            collection.drop();
        } catch (Exception e) {
            System.out.println("\nAn error occurred while dropping the " + databaseName + "." + tableName + " table in MongoDB\n");
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

                    // nem jo indextablaba-ba vagyunk
                    for (int index : indexOfFields) {
                        if (index < 0) {
                            return;
                        }
                    }

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
                System.out.println("\nAn error occurred while finding the index of the " + indexOfField + " attribute in " + fileName + "\n");
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
            System.out.println(nr);
            System.out.println(message.length);
            if ((message.length - 4) % nr != 0) {
                System.out.println("\nAn error occurred while inserting the " + tableName + " table in " + databaseName + " database.\n");
                return;
            }

            int nrPK = catalogHandler.getNumberOfPrimaryKeys(databaseName, tableName);
            int i = 4, rowNr = 0;
            nr -= nrPK;

            StringBuilder id = new StringBuilder();
            StringBuilder value = new StringBuilder();
            while (i < message.length) {
                if ((i - 4) == rowNr * (nr + nrPK)) {
                    id = new StringBuilder();
                    value = new StringBuilder();
                }
                id.append(message[i]);
                for (int j = 1; j < nrPK; j++) {
                    id.append("#").append(message[i + j]);
                }
                i += nrPK;

                if (i < message.length) {
                    value.append(message[i]);
                    for (int j = 1; j < nr; j++) {
                        value.append("#").append(message[i + j]);
                    }
                    i += nr;
                    rowNr++;

                    int nrOfUniqueKeys = catalogHandler.getNumberOfUniqueKeys(databaseName, tableName);
                    if (nrOfUniqueKeys != 0) {
                        StringBuilder unique = new StringBuilder();
                        for (int k = 0; k < nrOfUniqueKeys; k++) {
                            unique.append(value.toString().split("#")[k]);
                        }
                        unique = new StringBuilder(String.join("#", unique.toString()));
                        AtomicBoolean atomicBoolean = existsIndex(unique.toString(), databaseName, tableName);
                        if (atomicBoolean.get()) {
                            System.out.println("\nUnique key(s): " + unique + " already defined\n");
                            continue;
                        }
                    }
                    if (collection.find(new Document("_id", id.toString())).first() != null) {
                        System.out.println("\n[0 rows affected]\nThe document with id: " + id + " already exists in " + databaseName + "." + tableName + " table\n");
                    } else {
                        collection.insertOne(new Document("_id", id.toString()).append("value", value.toString()));
                        System.out.println("\nSuccessfully inserted into " + databaseName + "." + tableName + " with id: " + id + " values: " + value);
                        System.out.println(id.toString() + " : " + value.toString());
                        updateIndex(id.toString(), value.toString(), databaseName, tableName, "insert");
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("\nAn error occurred while inserting into the " + databaseName + "." + tableName + " table in MongoDB\n");
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
                    System.out.println("\nThe document with id: " + id + " can not be deleted because it is referenced by a foreign key constraint in another table.");
                } else {
                    System.out.println("\nSuccessfully deleted the document with id: " + id + " from the " + databaseName + "." + tableName + " table.");
                    table.deleteOne(document);
                }
            } else {
                System.out.println("\nThe document with id: " + id + " does not exists in " + databaseName + "." + tableName + " table\n");
            }
        } catch (Exception e) {
            System.out.println("\nAn error occured while deleting from " + databaseName + "." + tableName + " table in MongoDB\n");
            e.printStackTrace();
        }
    }

    private void normalSelect(String databaseName, String tableName, ArrayList<String> compareFields, String groupByField, ArrayList<String> aggregates) {
        try {
            MongoDatabase database = mongoClient.getDatabase(databaseName);
            MongoCollection<Document> table = database.getCollection(tableName);

            int nrOfAttributes = catalogHandler.getNumberOfAttributes(databaseName, tableName);
            int nrOfPrimaryKeys = catalogHandler.getNumberOfPrimaryKeys(databaseName, tableName);
            String[] stringOfAttributes = catalogHandler.getStringOfAttributes(databaseName, tableName).split(" ");
            selectFields = (Objects.equals(selectFields.get(0), "*")) ? new ArrayList<>(Arrays.asList(stringOfAttributes)) : selectFields;
//            int[] indexes = (Objects.equals(selectFields.get(0), "*")) ? IntStream.rangeClosed(0, nrOfAttributes - 1).toArray() : selectFields.stream().mapToInt(field -> (catalogHandler.getIndexOfAttribute(databaseName, tableName, field))).toArray();
//            System.out.println(Arrays.toString(indexes));

            List<Dictionary<String, String>> dictionaries = new ArrayList<>();
            String indexNames = catalogHandler.getIndexNames(databaseName);
            HashSet<String> remainedCompareFields = new HashSet<>();
            compareFields.forEach((compareField) -> {
                // condition[0] - field, condition[1] - operator, condition[2] - compareTo
                String[] condition = compareField.split(" ");
                if (Objects.equals(condition[1], "=")) {
                    Arrays.stream(indexNames.split(" ")).forEach(indexName -> {
                        if (indexName.contains(tableName)) {
                            if (indexName.contains(condition[0])) {
                                MongoCollection<Document> indexTable = database.getCollection(indexName);

                                FindIterable<Document> documents = indexTable.find(Filters.eq("_id", condition[2]));
                                for (Document document : documents) {
                                    Arrays.stream(document.get("value").toString().split("#")).forEach((id) -> {
                                        String[] values = Objects.requireNonNull(table.find(Filters.eq("_id", id)).first()).get("value").toString().split("#");

                                        Dictionary<String, String> dictionary = new Hashtable<>();
                                        for (int i = 0; i < nrOfAttributes; i++) {
                                            if (i < nrOfPrimaryKeys) {
                                                dictionary.put(stringOfAttributes[i], id.split("#")[i]);
                                            } else {
                                                dictionary.put(stringOfAttributes[i], values[i - nrOfPrimaryKeys]);
                                            }
                                        }
                                        dictionaries.add(dictionary);
                                    });
                                }
                            } else {
                                remainedCompareFields.add(compareField);
                            }
                        }
                    });
                } else {
                    remainedCompareFields.add(compareField);
                }
            });

            if (!dictionaries.isEmpty()) {
                Iterator<Dictionary<String, String>> iterator = dictionaries.iterator();
                while (iterator.hasNext()) {
                    Dictionary<String, String> dictionary = iterator.next();
                    boolean isOkDocumentary = true;
                    for (String compareField : remainedCompareFields) {
                        // condition[0] - field, condition[1] - operator, condition[2] - compareTo
                        String[] condition = compareField.split(" ");

                        boolean aux = false;
                        switch (condition[1]) {
                            case "=" -> aux = Objects.equals(dictionary.get(condition[0]), condition[2]);
                            case ">" ->
                                    aux = Integer.parseInt(dictionary.get(condition[0])) > Integer.parseInt(condition[2]);
                            case ">=" ->
                                    aux = Integer.parseInt(dictionary.get(condition[0])) >= Integer.parseInt(condition[2]);
                            case "<" ->
                                    aux = Integer.parseInt(dictionary.get(condition[0])) < Integer.parseInt(condition[2]);
                            case "<=" ->
                                    aux = Integer.parseInt(dictionary.get(condition[0])) <= Integer.parseInt(condition[2]);
                        }
                        if (!aux) {
                            isOkDocumentary = false;
                            break;
                        }
                    }
                    if (!isOkDocumentary) {
                        iterator.remove();
                        break;
                    }
                }
            } else {
                table.find().forEach((document) -> {
                    String[] values = Objects.requireNonNull(document.get("value")).toString().split("#");
                    String[] id = Objects.requireNonNull(document.get("_id")).toString().split("#");
                    boolean isOkDocument = true;

                    for (String compareField : compareFields) {
                        // condition[0] - field, condition[1] - operator, condition[2] - compareTo
                        String[] condition = compareField.split(" ");
                        int indexOfConditionField = catalogHandler.getIndexOfAttribute(databaseName, tableName, condition[0]);
                        boolean aux = false;
                        switch (condition[1]) {
                            case "=" -> {
                                if (indexOfConditionField < nrOfPrimaryKeys) {
                                    System.out.println(id[indexOfConditionField] + " = " + condition[2]);
                                } else {
                                    System.out.println(values[indexOfConditionField - nrOfPrimaryKeys] + " = " + condition[2]);
                                }
                                aux = (indexOfConditionField < nrOfPrimaryKeys) ? Objects.equals(id[indexOfConditionField], condition[2]) : Objects.equals(values[indexOfConditionField - nrOfPrimaryKeys], condition[2]);
                            }
                            case ">" ->
                                    aux = (indexOfConditionField < nrOfPrimaryKeys) ? Integer.parseInt(id[indexOfConditionField]) > Integer.parseInt(condition[2]) : Integer.parseInt(values[indexOfConditionField - nrOfPrimaryKeys]) > Integer.parseInt(condition[2]);
                            case ">=" ->
                                    aux = (indexOfConditionField < nrOfPrimaryKeys) ? Integer.parseInt(id[indexOfConditionField]) >= Integer.parseInt(condition[2]) : Integer.parseInt(values[indexOfConditionField - nrOfPrimaryKeys]) >= Integer.parseInt(condition[2]);
                            case "<" ->
                                    aux = (indexOfConditionField < nrOfPrimaryKeys) ? Integer.parseInt(id[indexOfConditionField]) < Integer.parseInt(condition[2]) : Integer.parseInt(values[indexOfConditionField - nrOfPrimaryKeys]) < Integer.parseInt(condition[2]);
                            case "<=" ->
                                    aux = (indexOfConditionField < nrOfPrimaryKeys) ? Integer.parseInt(id[indexOfConditionField]) <= Integer.parseInt(condition[2]) : Integer.parseInt(values[indexOfConditionField - nrOfPrimaryKeys]) <= Integer.parseInt(condition[2]);
                        }
                        if (!aux) {
                            isOkDocument = false;
                            break;
                        }
                    }
                    if (isOkDocument) {
                        Dictionary<String, String> dictionary = new Hashtable<>();
                        for (int i = 0; i < nrOfAttributes; i++) {
                            if (i < nrOfPrimaryKeys) {
                                dictionary.put(stringOfAttributes[i], id[i]);
                            } else {
                                dictionary.put(stringOfAttributes[i], values[i - nrOfPrimaryKeys]);
                            }
                        }
                        dictionaries.add(dictionary);
                    }
                });
            }

            if (!Objects.equals(groupByField, "")) {
                Map<String, List<Dictionary<String, String>>> groupedData = new HashMap<>();
                for (Dictionary<String, String> dictionary : dictionaries) {
                    String key = dictionary.get(groupByField);
                    if (groupedData.containsKey(key)) {
                        groupedData.get(key).add(dictionary);
                    } else {
                        List<Dictionary<String, String>> newList = new ArrayList<>();
                        newList.add(dictionary);
                        groupedData.put(key, newList);
                    }
                }

                if (!aggregates.isEmpty()) {
                    List<Dictionary<String, String>> newDictionaries = new ArrayList<>();
                    for (String key : groupedData.keySet()) {
                        List<Dictionary<String, String>> list = groupedData.get(key);
                        for (String s : aggregates) {
                            String[] parts = s.split(" ");
                            String fieldName = parts[0];   // field OR alias.field
                            String fieldType = parts[1];
                            String aggregate = parts[2];
                            String result = aggregateFunction(list, fieldName, fieldType, aggregate);
                            if (aggregate.equals("MIN") || aggregate.equals("MAX") || aggregate.equals("COUNT")) {
                                for (Dictionary<String, String> dictionary : list) {
                                    if (Objects.equals(dictionary.get(fieldName), result)) {
                                        dictionary.put((aggregate + "(" + fieldName + ")"), result);
                                        newDictionaries.add(dictionary);
                                        break;
                                    }
                                }
                            }
                            if (aggregate.equals("COUNT") || aggregate.equals("AVG") || aggregate.equals("SUM")) {
                                Dictionary<String, String> aggregateDictionary = list.get(0);
                                aggregateDictionary.put((aggregate + "(" + fieldName + ")"), result);
                                newDictionaries.add(aggregateDictionary);
                            }
                        }
                    }
                    for (Dictionary<String, String> dictionary : newDictionaries) {
                        for (String field : selectFields) {
                            String value = dictionary.get(field);
                            System.out.println("Field: " + field + ", Value: " + value);
                        }
                        System.out.println();
                    }
                } else {
                    for (Map.Entry<String, List<Dictionary<String, String>>> entry : groupedData.entrySet()) {
                        String key = entry.getKey(); // Get the key of the current entry
                        List<Dictionary<String, String>> values = entry.getValue(); // Get the list of values for the current key

                        System.out.println("Key: " + key);

                        // Iterate over the list of values
                        for (Dictionary<String, String> dictionary : values) {
                            // Iterate over the entries in the dictionary
                            Enumeration<String> dictKeys = dictionary.keys();
                            while (dictKeys.hasMoreElements()) {
                                String dictKey = dictKeys.nextElement();
                                String dictValue = dictionary.get(dictKey);
                                System.out.println("  " + dictKey + ": " + dictValue);
                            }
                        }
                    }
                }
            } else {
                if (!aggregates.isEmpty()) {
                    List<Dictionary<String, String>> newDictionaries = new ArrayList<>();
                    for (String s : aggregates) {
                        String[] parts = s.split(" ");
                        String fieldName = parts[0];   // field OR alias.field
                        String fieldType = parts[1];
                        String aggregate = parts[2];
                        String result = aggregateFunction(dictionaries, fieldName, fieldType, aggregate);
                        if (aggregate.equals("MIN") || aggregate.equals("MAX") || aggregate.equals("COUNT")) {
                            for (Dictionary<String, String> dictionary : dictionaries) {
                                if (Objects.equals(dictionary.get(fieldName), result)) {
                                    dictionary.put((aggregate + "(" + fieldName + ")"), result);
                                    newDictionaries.add(dictionary);
                                    break;
                                }
                            }
                        }
                    }
                    for (Dictionary<String, String> dictionary : newDictionaries) {
                        for (String field : selectFields) {
                            String value = dictionary.get(field);
                            System.out.println("Field: " + field + ", Value: " + value);
                        }
                        System.out.println();
                    }
                }else{
                    for (Dictionary<String, String> dictionary : dictionaries) {
                        for (String field : selectFields) {
                            String value = dictionary.get(field);
                            System.out.println("Field: " + field + ", Value: " + value);
                        }
                        System.out.println();
                    }
                }
            }

//            StringBuilder fieldNames = new StringBuilder();
//            fieldNames.append(" ").append("#");
//            for (String field : selectFields) {
//                fieldNames.append(field).append("#");
//            }
//
//            fieldNames.deleteCharAt(fieldNames.length() - 1);
//            writeIntoSocket(fieldNames.toString());
//
//            int nr = 1;
//            StringBuilder values = new StringBuilder();
//            for (Dictionary<String, String> dictionary : dictionaries) {
//                values.append(nr).append(" ");
//                nr++;
//                for (String field : selectFields) {
//                    values.append(dictionary.get(field)).append(" ");
//                }
//                values.deleteCharAt(values.length() - 1);
//                values.append("#");
//            }
//            if (values.length() == 0) {
//                writeIntoSocket("");
//                return;
//            }
//            values.deleteCharAt(values.length() - 1);
//            writeIntoSocket(values.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void select(String[] message) {
        try {
            List<Dictionary<String, String>> dictionaries = new ArrayList<>();
            String databaseName, tableName, tableAlias = "";
            selectFields = new ArrayList<>();
            ArrayList<String> aggregates = new ArrayList<>();
            HashMap<String, String> aliasTables = new HashMap<>();

            int i = 1;
            while (!Objects.equals(message[i], "FROM")) {
                if (Objects.equals(message[i], "MIN") || Objects.equals(message[i], "MAX") || Objects.equals(message[i], "AVG") || Objects.equals(message[i], "COUNT") || Objects.equals(message[i], "SUM")) {
                    aggregates.add(message[i + 1] + "." + message[i]);
                    selectFields.add(message[i] + "(" + message[i + 1] + ")");
                    i += 2;
                } else {
                    selectFields.add(message[i++]);
                }
            }
            i++; //FROM

            String[] parts = message[i++].split("\\.");
            databaseName = parts[0];
            tableName = parts[1];

            if (i < message.length && (!Objects.equals(message[i], "INNER") && !Objects.equals(message[i], "WHERE") && !Objects.equals(message[i], "GROUP"))) {
                tableAlias = message[i++];
                aliasTables.put(tableAlias, tableName);
            }

            boolean wasJoin = false;
            ArrayList<String> joinThem = new ArrayList<>(); // minden sora: joinDatabaseName, joinTableName, joinAliasTable, joinfieldName, fieldName
            if (i < message.length && Objects.equals(message[i], "INNER")) {
                wasJoin = true;
                while (i < message.length && Objects.equals(message[i], "INNER")) {
                    i += 2;   // INNER JOIN
                    parts = message[i++].split("\\.");
                    String joinDatabaseName = (parts.length == 1) ? databaseName : parts[0];
                    String joinTableName = (parts.length == 1) ? parts[0] : parts[1], joinTableAlias = "";
                    if (!Objects.equals(message[i], "ON")) {
                        joinTableAlias = message[i++];
                        if (aliasTables.containsKey(joinTableAlias)) {
                            System.out.println("An error1 occurred while performing Select statement!\n");
                            return;
                        }
                        aliasTables.put(joinTableAlias, joinTableName);
                    }
                    i += 1;   // ON

                    String[] parts1 = message[i++].split("\\.");
                    String condition = message[i++];
                    String[] parts2 = message[i++].split("\\.");
                    if (!aliasTables.containsKey(parts1[0]) || !aliasTables.containsKey(parts2[0])) {
                        System.out.println("An error2 occurred while performing Select statement!\n");
                        return;
                    }

                    if (Objects.equals(tableAlias, parts1[0])) {
                        joinThem.add(joinDatabaseName + " " + joinTableName + " " + parts2[0] + " " + parts2[1] + " " + parts1[1]);
                    } else if (Objects.equals(tableAlias, parts2[0])) {
                        joinThem.add(joinDatabaseName + " " + joinTableName + " " + parts1[0] + " " + parts1[1] + " " + parts2[1]);
                    } else {
                        System.out.println("An error3 occurred while performing Select statement!\n");
                        return;
                    }
                }
                dictionaries = joinTables(databaseName, tableName, tableAlias, joinThem);
            }

            ArrayList<String> compareFields = new ArrayList<>();
            if (i < message.length && Objects.equals(message[i], "WHERE")) {
                while (i < message.length && (Objects.equals(message[i], "WHERE") || Objects.equals(message[i], "AND"))) {
                    i++;
                    String[] parts1 = message[i++].split("\\.");
                    String condition = message[i++];
                    String parts2 = message[i++];
                    if (parts1.length == 1) {
                        compareFields.add(parts1[0] + " " + condition + " " + parts2);
                    } else {
                        if (!aliasTables.containsKey(parts1[0])) {
                            System.out.println("An error occurred while performing Select statement!\n");
                            return;
                        }
                        compareFields.add(parts1[0] + "." + parts1[1] + " " + condition + " " + parts2);
                    }
                }
            }

            String groupByField = "";
            if (i < message.length && Objects.equals(message[i], "GROUP")) {
                i += 2; // GROUP BY
                groupByField = message[i++];
            }

            if (!wasJoin) {
                ArrayList<String> finalAggregates = new ArrayList<>();
                for (String aggregate : aggregates) {
                    parts = aggregate.split("\\.");
                    String fieldType = catalogHandler.getAttributeType(databaseName, tableName, parts[0]);
                    finalAggregates.add(parts[0] + " " + fieldType + " " + parts[1]);
                }
                normalSelect(databaseName, tableName, compareFields, groupByField, finalAggregates);
            } else {
                ArrayList<String> finalAggregates = new ArrayList<>();
                for (String aggregate : aggregates) {
                    parts = aggregate.split("\\.");
                    String fieldType = catalogHandler.getAttributeType(databaseName, aliasTables.get(parts[0]), parts[1]);
                    finalAggregates.add(parts[0] + "." + parts[1] + " " + fieldType + " " + parts[2]);
                }
                complexSelect(dictionaries, compareFields, groupByField, finalAggregates);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void complexSelect
            (List<Dictionary<String, String>> dictionaries, ArrayList<String> compareFields, String
                    groupByField, ArrayList<String> aggregates) {
        List<Dictionary<String, String>> newDictionaries = new ArrayList<>();
        for (Dictionary<String, String> dictionary : dictionaries) {
            boolean isDictionaryGood = true;
            for (String compareField : compareFields) {
                // condition[0] - field, condition[1] - operator, condition[2] - compareTo
                String[] condition = compareField.split(" ");
                if (!Objects.equals(dictionary.get(condition[0]), condition[2])) {
                    isDictionaryGood = false;
                    break;
                }
            }
            if (isDictionaryGood) {
                newDictionaries.add(dictionary);
            }
        }

        if (!Objects.equals(groupByField, "")) {
            Map<String, List<Dictionary<String, String>>> groupedData = new HashMap<>();
            for (Dictionary<String, String> dictionary : newDictionaries) {
                String key = dictionary.get(groupByField);
                if (groupedData.containsKey(key)) {
                    groupedData.get(key).add(dictionary);
                } else {
                    List<Dictionary<String, String>> newList = new ArrayList<>();
                    newList.add(dictionary);
                    groupedData.put(key, newList);
                }
            }


            if (!aggregates.isEmpty()) {
                List<Dictionary<String, String>> newestDictionaries = new ArrayList<>();
                for (String key : groupedData.keySet()) {
                    List<Dictionary<String, String>> list = groupedData.get(key);
                    for (String s : aggregates) {
                        String[] parts = s.split(" ");
                        String fieldName = parts[0];   // field OR alias.field
                        String fieldType = parts[1];
                        String aggregate = parts[2];
                        String result = aggregateFunction(list, fieldName, fieldType, aggregate);
                        if (aggregate.equals("MIN") || aggregate.equals("MAX")) {
                            for (Dictionary<String, String> dictionary : list) {
                                if (Objects.equals(dictionary.get(fieldName), result)) {
                                    dictionary.put((aggregate + "(" + fieldName + ")"), result);
                                    newestDictionaries.add(dictionary);
                                    break;
                                }
                            }
                        } else if (aggregate.equals("COUNT") || aggregate.equals("AVG") || aggregate.equals("SUM")) {
                            Dictionary<String, String> aggregateDictionary = list.get(0);
                            aggregateDictionary.put((aggregate + "(" + fieldName + ")"), result);
                            newestDictionaries.add(aggregateDictionary);
                        }
                    }
                }
                for (Dictionary<String, String> dictionary : newestDictionaries) {
                    for (String field : selectFields) {
                        String value = dictionary.get(field);
                        System.out.println("Field: " + field + ", Value: " + value);
                    }
                    System.out.println();
                }
            } else {
                for (Map.Entry<String, List<Dictionary<String, String>>> entry : groupedData.entrySet()) {
                    String key = entry.getKey(); // Get the key of the current entry
                    List<Dictionary<String, String>> values = entry.getValue(); // Get the list of values for the current key

                    System.out.println("Key: " + key);

                    // Iterate over the list of values
                    for (Dictionary<String, String> dictionary : values) {
                        // Iterate over the entries in the dictionary
                        Enumeration<String> dictKeys = dictionary.keys();
                        while (dictKeys.hasMoreElements()) {
                            String dictKey = dictKeys.nextElement();
                            String dictValue = dictionary.get(dictKey);
                            System.out.println("  " + dictKey + ": " + dictValue);
                        }
                    }
                }
            }
        } else {
            for (Dictionary<String, String> dictionary : newDictionaries) {
                for (String field : selectFields) {
                    String value = dictionary.get(field);
                    System.out.println("Field: " + field + ", Value: " + value);
                }
                System.out.println();
            }
        }

//        StringBuilder fieldNames = new StringBuilder();
//        fieldNames.append(" ").append("#");
//        for (String field : selectFields) {
//            fieldNames.append(field).append("#");
//        }
//
//        fieldNames.deleteCharAt(fieldNames.length() - 1);
//        writeIntoSocket(fieldNames.toString());
//
//        int nr = 1;
//        StringBuilder values = new StringBuilder();
//        for (Dictionary<String, String> dictionary : newDictionaries) {
//            values.append(nr).append(" ");
//            nr++;
//            for (String field : selectFields) {
//                values.append(dictionary.get(field)).append(" ");
//            }
//            values.deleteCharAt(values.length() - 1);
//            values.append("#");
//        }
//        if (values.length() == 0) {
//            writeIntoSocket("");
//            return;
//        }
//        values.deleteCharAt(values.length() - 1);
//        System.out.println(values.toString());
//        writeIntoSocket(values.toString());
//
//
//        System.out.println("------------------------------------");
//        for (Dictionary<String, String> dictionary : newDictionaries) {
//            for (String field : selectFields) {
//                String value = dictionary.get(field);
//
//                System.out.println("Field: " + field + ", Value: " + value);
//            }
//            System.out.println();
//        }
    }

    private List<Dictionary<String, String>> joinTables(String databaseName, String tableName, String
            aliasTable, ArrayList<String> joinThem) {
        MongoDatabase database = mongoClient.getDatabase(databaseName);
        MongoCollection<Document> table = database.getCollection(tableName);
        FindIterable<Document> documents = table.find();

        String indexNames = catalogHandler.getIndexNames(databaseName);
        int nrOfAttributes = catalogHandler.getNumberOfAttributes(databaseName, tableName);
        int nrOfPrimaryKeys = catalogHandler.getNumberOfPrimaryKeys(databaseName, tableName);
        String[] stringOfAttributes = catalogHandler.getStringOfAttributes(databaseName, tableName).split(" ");

        boolean everyField = Objects.equals(selectFields.get(0), "*");
        if (everyField) {
            selectFields.clear();
            Arrays.stream(stringOfAttributes).forEach(field -> selectFields.add(aliasTable + "." + field));
        }

        List<Dictionary<String, String>> dictionaries = new ArrayList<>();
        // join[0] - joinDatabaseName, join[1] - joinTableName, join[2] - joinAliasTable, join[3] - joinFieldName, join[4] - fieldName
        String joinDatabaseName = joinThem.get(0).split(" ")[0];
        String joinTableName = joinThem.get(0).split(" ")[1];
        String joinAliasTable = joinThem.get(0).split(" ")[2];
        String joinFieldName = joinThem.get(0).split(" ")[3];
        String fieldName = joinThem.get(0).split(" ")[4];

        MongoDatabase joinDatabase = (Objects.equals(databaseName, joinDatabaseName)) ? database : mongoClient.getDatabase(joinDatabaseName);
        MongoCollection<Document> joinTable = joinDatabase.getCollection(joinTableName);

        int indexJoinField = catalogHandler.getIndexOfAttribute(joinDatabaseName, joinTableName, joinFieldName);
        int nrOfJoinPrimaryKeys = catalogHandler.getNumberOfPrimaryKeys(joinDatabaseName, joinTableName);
        int nrOfJoinAttributes = catalogHandler.getNumberOfAttributes(joinDatabaseName, joinTableName);
        String[] stringOfJoinAttributes = catalogHandler.getStringOfAttributes(joinDatabaseName, joinTableName).split(" ");

        if (everyField) {
            String finalJoinAliasTable = joinAliasTable;
            Arrays.stream(stringOfJoinAttributes).forEach(joinField -> selectFields.add(finalJoinAliasTable + "." + joinField));
        }

        int indexField = catalogHandler.getIndexOfAttribute(databaseName, tableName, fieldName);

        FindIterable<Document> joinDocuments = joinTable.find();
        for (Document joinDocument : joinDocuments) {
            String refValue = (indexJoinField < nrOfJoinPrimaryKeys) ? joinDocument.get("_id").toString().split("#")[indexJoinField] : joinDocument.get("value").toString().split("#")[indexJoinField - nrOfJoinPrimaryKeys];
            for (Document document : documents) {
                String value = (indexField < nrOfPrimaryKeys) ? document.get("_id").toString().split("#")[indexField] : document.get("value").toString().split("#")[indexField - nrOfPrimaryKeys];

                if (Objects.equals(refValue, value)) {
                    Dictionary<String, String> dictionary = new Hashtable<>();

                    for (int i = 0; i < nrOfAttributes; i++) {
                        if (i < nrOfPrimaryKeys) {
                            dictionary.put((aliasTable + "." + stringOfAttributes[i]), document.get("_id").toString().split("#")[i]);
                        } else {
                            dictionary.put((aliasTable + "." + stringOfAttributes[i]), document.get("value").toString().split("#")[i - nrOfPrimaryKeys]);
                        }
                    }

                    for (int i = 0; i < nrOfJoinAttributes; i++) {
                        if (i < nrOfJoinPrimaryKeys) {
                            dictionary.put((joinAliasTable + "." + stringOfJoinAttributes[i]), joinDocument.get("_id").toString().split("#")[i]);
                        } else {
                            dictionary.put((joinAliasTable + "." + stringOfJoinAttributes[i]), joinDocument.get("value").toString().split("#")[i - nrOfJoinPrimaryKeys]);
                        }
                    }

                    dictionaries.add(dictionary);
                }
            }
        }


        if (!joinThem.isEmpty()) {
            for (int k = 1; k < joinThem.size(); k++) {
                String join = joinThem.get(k);
                Iterator<Dictionary<String, String>> iterator = dictionaries.iterator();
                while (iterator.hasNext()) {
                    Dictionary<String, String> dictionary = iterator.next();
                    Enumeration<String> keys = dictionary.keys();

                    // join[0] - joinDatabaseName, join[1] - joinTableName, join[2] - joinAliasTable, join[3] - joinFieldName, join[4] - fieldName
                    joinDatabaseName = join.split(" ")[0];
                    joinTableName = join.split(" ")[1];
                    joinAliasTable = join.split(" ")[2];
                    joinFieldName = join.split(" ")[3];
                    fieldName = join.split(" ")[4];


                    String targetKey = null;
                    Enumeration<String> targetKeys = dictionary.keys();
                    while (targetKeys.hasMoreElements()) {
                        String key = targetKeys.nextElement();
                        if (key.equals(aliasTable + "." + fieldName)) {
                            targetKey = key;
                            break;
                        }
                    }

                    String key = targetKey;    // "alias.field"
                    String value = dictionary.get(key); // "field's value"

                    joinDatabase = (Objects.equals(databaseName, joinDatabaseName)) ? database : mongoClient.getDatabase(joinDatabaseName);
                    joinTable = joinDatabase.getCollection(joinTableName);

                    indexJoinField = catalogHandler.getIndexOfAttribute(joinDatabaseName, joinTableName, joinFieldName);
                    nrOfJoinPrimaryKeys = catalogHandler.getNumberOfPrimaryKeys(joinDatabaseName, joinTableName);
                    nrOfJoinAttributes = catalogHandler.getNumberOfAttributes(joinDatabaseName, joinTableName);
                    stringOfJoinAttributes = catalogHandler.getStringOfAttributes(joinDatabaseName, joinTableName).split(" ");

                    if (everyField) {
                        String finalJoinAliasTable = joinAliasTable;
                        Arrays.stream(stringOfJoinAttributes).forEach(joinField -> selectFields.add(finalJoinAliasTable + "." + joinField));
                    }

                    boolean foundMatchingValue = false;
                    joinDocuments = joinTable.find();
                    for (Document joinDocument : joinDocuments) {
                        String refValue = (indexJoinField < nrOfJoinPrimaryKeys) ? joinDocument.get("_id").toString().split("#")[indexJoinField] : joinDocument.get("value").toString().split("#")[indexJoinField - nrOfJoinPrimaryKeys];

                        System.out.println(value + " " + refValue);
                        if (Objects.equals(value, refValue)) {
                            foundMatchingValue = true;

                            for (int i = 0; i < nrOfJoinAttributes; i++) {
                                if (i < nrOfJoinPrimaryKeys) {
                                    dictionary.put((joinAliasTable + "." + stringOfJoinAttributes[i]), joinDocument.get("_id").toString().split("#")[i]);
                                } else {
                                    dictionary.put((joinAliasTable + "." + stringOfJoinAttributes[i]), joinDocument.get("value").toString().split("#")[i - nrOfJoinPrimaryKeys]);
                                }
                            }
                        }
                    }

                    if (!foundMatchingValue) {
                        iterator.remove(); // Remove the current dictionary from the list
                    }
                }
            }
        }
        return dictionaries;
    }

    public String aggregateFunction(List<Dictionary<String, String>> dictionaries, String fieldName, String
            fieldType, String aggregate) {
        String result = "";

        ArrayList<String> values = new ArrayList<>();
        dictionaries.forEach((dictionary) -> values.add(dictionary.get(fieldName)));
        switch (aggregate) {
            case "MIN" -> {
                switch (fieldType) {
                    case "VARCHAR" -> {
                        String minimum = values.stream()
                                .min(String::compareTo)
                                .orElseThrow();
                        result = minimum;
                    }
                    case "INT" -> {
                        Integer minimum = values.stream()
                                .map(Integer::parseInt)
                                .min(Integer::compareTo)
                                .orElseThrow();
                        result = minimum.toString();
                    }
                    case "FLOAT" -> {
                        Float minimum = values.stream()
                                .map(Float::parseFloat)
                                .min(Float::compareTo)
                                .orElseThrow();
                        result = minimum.toString();
                    }
                    default -> {
                        System.out.println("You can not use the aggregate function: \"MIN\" for that type.");
                        return null;
                    }
                }
            }
            case "MAX" -> {
                switch (fieldType) {
                    case "VARCHAR" -> {
                        String minimum = values.stream()
                                .max(String::compareTo)
                                .orElseThrow();
                        result = minimum;
                    }
                    case "INT" -> {
                        Integer minimum = values.stream()
                                .map(Integer::parseInt)
                                .max(Integer::compareTo)
                                .orElseThrow();
                        result = minimum.toString();
                    }
                    case "FLOAT" -> {
                        Float minimum = values.stream()
                                .map(Float::parseFloat)
                                .max(Float::compareTo)
                                .orElseThrow();
                        result = minimum.toString();
                    }
                    default -> {
                        System.out.println("You can not use the aggregate function: \"MAX\" for that type.");
                        return null;
                    }
                }
            }
            case "AVG" -> {
                switch (fieldType) {
                    case "INT" -> {
                        double average = values.stream()
                                .mapToInt(Integer::parseInt)
                                .average()
                                .orElseThrow();
                        result = String.valueOf(average);
                    }
                    case "FLOAT" -> {
                        double average = values.stream()
                                .mapToDouble(Float::parseFloat)
                                .average()
                                .orElseThrow();
                        result = String.valueOf(average);
                    }
                    default -> {
                        System.out.println("You can not use the aggregate function: \"AVG\" for that type.");
                        return null;
                    }
                }
            }
            case "COUNT" -> {
                return String.valueOf(values.size());
            }
            case "SUM" -> {
                switch (fieldType) {
                    case "INT" -> {
                        int sum = values.stream()
                                .mapToInt(Integer::parseInt)
                                .sum();
                        result = String.valueOf(sum);
                    }
                    case "FLOAT" -> {
                        double sum = values.stream()
                                .mapToDouble(Float::parseFloat)
                                .sum();
                        result = String.valueOf(sum);
                    }
                    default -> {
                        System.out.println("You can not use the aggregate function: \"SUM\" for that type.");
                        return null;
                    }
                }
            }
        }
        return result;
    }
}
//                    Arrays.stream(indexNames.split(" ")).forEach(indexName -> {
//                    if (indexName.contains(tableName)) {
//                        if (indexName.contains(fieldName)) {
//                            System.out.println(fieldName + " <- van index\n");
//                            MongoCollection<Document> indexTable = database.getCollection(indexName);
//                            FindIterable<Document> documents = indexTable.find(Filters.eq("_id", refValue));
//
//
//                            for (Document document : documents) {
//                                Arrays.stream(document.get("value").toString().split("#")).forEach((id) -> {
//                                    String[] values = Objects.requireNonNull(table.find(Filters.eq("_id", id)).first()).get("value").toString().split("#");
//
//                                    Dictionary<String, String> dictionary = new Hashtable<>();
//                                    for (int i = 0; i < nrOfAttributes; i++) {
//                                        if (i < nrOfPrimaryKeys) {
//                                            dictionary.put(stringOfAttributes[i], id.split("#")[i]);
//                                        } else {
//                                            dictionary.put(stringOfAttributes[i], values[i - nrOfPrimaryKeys]);
//                                        }
//                                    }
//                                    dictionaries.add(dictionary);
//                                });
//                            }
//                        }
//                    }
//                });

//        if (dictionaries.isEmpty()) {
//            String tableName = aliasTables.get(parts1[0]);
//            String fieldName = parts1[1];
//            String joinTableName = aliasTables.get(parts2[0]);
//            String joinFieldName = parts2[1];
//            MongoCollection<Document> table = database.getCollection(tableName);
//            MongoCollection<Document> joinTable = joinDatabase.getCollection(joinTableName);
//
//            int nrOfAttributes = catalogHandler.getNumberOfAttributes(databaseName, tableName);
//            int nrOfPrimaryKeys = catalogHandler.getNumberOfPrimaryKeys(databaseName, tableName);
//            String[] stringOfAttributes = catalogHandler.getStringOfAttributes(databaseName, tableName).split(" ");
//
//
//            FindIterable<Document> joinDocuments = joinTable.find();
//            for (Document joinDocument : joinDocuments) {
//                FindIterable<Document> documents = table.find(Filters.eq("_id", joinDocument.get("_id")));
//                for (Document document : documents) {
//                    String[] fields = document.get("value").toString().concat("#").concat(joinDocument.getString("value")).split("#");
//
//                    Dictionary<String, String> dictionary = new Hashtable<>();
//                    for (int i = 0; i < nrOfAttributes; i++) {
//                        if (i < nrOfPrimaryKeys) {
////                            dictionary.put(stringOfAttributes[i], value.split("#")[i]);
//                        } else {
////                            dictionary.put(stringOfAttributes[i], values[i - nrOfPrimaryKeys]);
//                        }
//                    }
//                    dictionaries.add(dictionary);
//                }
//            }
//
//        }



