package edu.ubbcluj.ab2.minidb;

import com.mongodb.client.MongoClient;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class CatalogHandler {
    private Root root;
    private Root.Database database;
    private Root.Database.Index index;
    private Root.Database.Table table;
    private Root.Database.Table.Attribute attribute;
    private Root.Database.Table.PrimaryKey primaryKey;
    private Root.Database.Table.ForeignKey foreignKey;
    private Root.Database.Table.UniqueKey uniqueKey;
    private Root.Database.Table.ForeignKey.Reference reference;
    private MongoClient mongoClient;

    public CatalogHandler(String fileName, MongoClient mongoClient) {
        this.mongoClient = mongoClient;
        String jsonString;
        try {
            jsonString = new String(Files.readAllBytes(Paths.get(fileName)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.root = RootDeserializer.deserialize(jsonString);
    }

    public CatalogHandler(MongoClient mongoClient) {
        this.root = new Root();
        this.mongoClient = mongoClient;
    }

    public void saveCatalogToFile(String fileName) {
        try {
            FileWriter file = new FileWriter(fileName);
            file.append(root.toJsonObject().toString());
            file.close();
        } catch (IOException e) {
            System.out.println("An error occurred while writing to " + fileName + " file!\n");
            throw new RuntimeException(e);
        }
    }

    public void refreshContent(String fileName) {
        String jsonString;
        try {
            jsonString = new String(Files.readAllBytes(Paths.get(fileName)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.root = RootDeserializer.deserialize(jsonString);
    }

    public void createDatabase(String databaseName) {
        Root.Database d = getInstanceOfDatabase(databaseName);
        if (d != null) {
            System.out.println("The Database " + databaseName + " already exists!\n");
        } else {
            database = root.new Database(root, databaseName);
        }
    }

    public void dropDatabase(String databaseName) {
        Root.Database d = getInstanceOfDatabase(databaseName);
        if (d != null) {
            root.databases.remove(d);
        } else {
            System.out.println("The Database " + databaseName + " does not exist!\n");
        }
    }

//    public void createIndex(String databaseName, String indexName, String fields) {
//        Root.Database d = this.getInstanceOfDatabase(databaseName);
//        if (d == null) {
//            System.out.println("The database " + databaseName + " does not exist\n");
//            return;
//        }
//        Root.Database.Index i = this.getInstanceOfIndex(databaseName, indexName);
//        if (i != null) {
//            System.out.println("The index " + indexName + " already exists\n");
//            return;
//        }
//        index = d.new Index(d, indexName, fields.split("#"));
//    }

    public void createIndex(String databaseName, String indexName, String fields) {
        Root.Database d = this.getInstanceOfDatabase(databaseName);
        if (d != null) {
            Root.Database.Index i = getInstanceOfIndex(databaseName, indexName);
            if (i != null) {
                System.out.println("ERROR at creating the Index!");
                System.out.println("The Index " + indexName + " already exists!");
            } else {
                String[] aux =  fields.split(" ");
                index = d.new Index(d, indexName, aux);
            }
        } else {
            System.out.println("ERROR at creating the Table!");
            System.out.println("The Database " + databaseName + " does not exist!\n");
        }

    }

    public boolean existsIndex(String databaseName, String indexName) {
        Root.Database d = getInstanceOfDatabase(databaseName);
        if (d == null) {
            System.out.println("The Database " + databaseName + " does not exist!\n");
            return false;
        }
        return getInstanceOfIndex(databaseName, indexName) != null;
    }

    public String getIndexNames(String databaseName) {
        Root.Database d = getInstanceOfDatabase(databaseName);
        if (d == null) {
            System.out.println("The Database " + databaseName + " does not exist!\n");
            return "";
        }
        String indexNames = "";
        for (Root.Database.Index i : d.indexes) {
            indexNames += i.indexName + " ";
        }
        return indexNames;
    }

    // return: "field11#field12 field21#field22#field23..."
    public String getIndexFields(String databaseName) {
        Root.Database d = getInstanceOfDatabase(databaseName);
        if (d == null) {
            System.out.println("The Database " + databaseName + " does not exist!\n");
            return "";
        }
        StringBuilder indexFields = new StringBuilder();
        for (Root.Database.Index i : d.indexes) {
            indexFields.append(String.join("#", i.fields)).append(" ");
        }
        return indexFields.toString();
    }

    public String getCertainIndexFields(String databaseName, String indexName) {
        Root.Database d = this.getInstanceOfDatabase(databaseName);
        if (d == null) {
            System.out.println("The database " + databaseName + " does not exist\n");
            return "";
        }
        Root.Database.Index i = this.getInstanceOfIndex(databaseName, indexName);
        if (i == null) {
            System.out.println("The certain index " + indexName + " does not exist\n");
            return "";
        }
        return String.join("#", i.fields);
    }

    public void removeIndex(String databaseName, String indexName) {
        Root.Database d = getInstanceOfDatabase(databaseName);
        if (d != null) {
            d.indexes.remove(indexName);
        } else {
            System.out.println("An error occurred while removing the " + indexName + " index from " + databaseName + " database.\n");
            System.out.println("The Database " + databaseName + " does not exist!\n");
        }
    }

    public void createTable(String databaseName, String tableName) {
        Root.Database d = this.getInstanceOfDatabase(databaseName);
        if (d != null) {
            Root.Database.Table t = getInstanceOfTable(databaseName, tableName);
            if (t != null) {
                System.out.println("ERROR at creating the Table!");
                System.out.println("The Table" + databaseName + "." + tableName + "already exists!");
            } else {
                table = d.new Table(d, tableName);
            }
        } else {
            System.out.println("ERROR at creating the Table!");
            System.out.println("The Database " + databaseName + " does not exist!\n");
        }
    }

    public void dropTable(String databaseName, String tableName) {
        Root.Database d = this.getInstanceOfDatabase(databaseName);
        if (d != null) {
            Root.Database.Table t = this.getInstanceOfTable(databaseName, tableName);
            if (t != null) {
                d.tables.remove(t);
            } else {
                System.out.println("ERROR at dropping the Table!");
                System.out.println("The Table " + databaseName + "." + tableName + " does not exist!\n");
            }
        } else {
            System.out.println("ERROR at dropping the Table!");
            System.out.println("The Database " + databaseName + " does not exist!\n");
        }
    }

    public void createAttribute(String databaseName, String tableName, String attributeName, String attributeType) {
        Root.Database.Table t = this.getInstanceOfTable(databaseName, tableName);
        if (t != null) {
            Root.Database.Table.Attribute a = getInstanceOfAttribute(databaseName, tableName, attributeName, attributeType);
            if (a != null) {
                System.out.println("ERROR at creating the Attribute!");
                System.out.println("The Attribute " + databaseName + "." + tableName + "." + attributeName + "already exists!");
            } else {
                attribute = t.new Attribute(t, attributeName, attributeType);
            }
        } else {
            System.out.println("ERROR at creating the Attribute!");
            System.out.println("The Table " + databaseName + "." + tableName + " does not exist!\n");
        }
    }

    public void deleteAttribute(String databaseName, String tableName, String attributeName, String attributeType) {
        Root.Database.Table t = getInstanceOfTable(databaseName, tableName);
        if (t != null) {
            Root.Database.Table.Attribute a = getInstanceOfAttribute(databaseName, tableName, attributeName, attributeType);
            if (a != null) {
                t.attributes.remove(a);
            } else {
                System.out.println("ERROR at deleting the Attribute!");
                System.out.println("The Attribute " + databaseName + "." + tableName + "." + attributeName + " does not exist!\n");
            }
        } else {
            System.out.println("ERROR at deleting the Attribute!");
            System.out.println("The Table " + databaseName + "." + tableName + " does not exist!\n");
        }
    }

    public void createPrimaryKey(String databaseName, String tableName, String attributeName) {
        Root.Database.Table t = this.getInstanceOfTable(databaseName, tableName);
        if (t != null) {
            Root.Database.Table.PrimaryKey p = getInstanceOfPrimaryKey(databaseName, tableName, attributeName);
            if (p != null) {
                System.out.println("ERROR at creating the Primary Key!");
                System.out.println("The Primary Key " + databaseName + "." + tableName + "." + attributeName + "already exists!");
            } else {
                primaryKey = t.new PrimaryKey(t, attributeName);
            }
        } else {
            System.out.println("ERROR at creating the Primary Key!");
            System.out.println("The Table " + databaseName + "." + tableName + " does not exist!\n");
        }
    }

    public void deletePrimaryKey(String databaseName, String tableName, String attributeName) {
        Root.Database.Table t = getInstanceOfTable(databaseName, tableName);
        if (t != null) {
            Root.Database.Table.PrimaryKey p = getInstanceOfPrimaryKey(databaseName, tableName, attributeName);
            if (p != null) {
                t.primaryKeys.remove(p);
            } else {
                System.out.println("ERROR at deleting the Primary Key!");
                System.out.println("The Primary Key " + databaseName + "." + tableName + "." + attributeName + " does not exist!\n");
            }
        } else {
            System.out.println("ERROR at deleting the Primary Key!");
            System.out.println("The Table " + databaseName + "." + tableName + " does not exist!\n");
        }
    }

    public void createForeignKey(String databaseName, String tableName, String attributeName) {
        Root.Database.Table t = this.getInstanceOfTable(databaseName, tableName);
        if (t != null) {
            Root.Database.Table.ForeignKey f = getInstanceOfForeignKey(databaseName, tableName, attributeName);
            if (f != null) {
                System.out.println("ERROR at creating the Foreign Key!");
                System.out.println("The Foreign Key " + databaseName + "." + tableName + "." + attributeName + "already exists!");
            } else {
                foreignKey = t.new ForeignKey(t, attributeName);
            }
        } else {
            System.out.println("ERROR at creating the Foreign Key!");
            System.out.println("The Table " + databaseName + "." + tableName + " does not exist!\n");
        }
    }

    public void deleteForeignKey(String databaseName, String tableName, String attributeName) {
        Root.Database.Table t = getInstanceOfTable(databaseName, tableName);
        if (t != null) {
            Root.Database.Table.ForeignKey f = getInstanceOfForeignKey(databaseName, tableName, attributeName);
            if (f != null) {
                t.foreignKeys.remove(f);
            } else {
                System.out.println("ERROR at deleting the Foreign Key!");
                System.out.println("The Foreign Key " + databaseName + "." + tableName + "." + attributeName + " does not exist!\n");
            }
        } else {
            System.out.println("ERROR at deleting the Foreign Key!");
            System.out.println("The Table " + databaseName + "." + tableName + " does not exist!\n");
        }
    }

    public void createUnique(String databaseName, String tableName, String attributeName) {
        Root.Database.Table t = this.getInstanceOfTable(databaseName, tableName);
        if (t != null) {
            Root.Database.Table.UniqueKey u = getInstanceOfUnique(databaseName, tableName, attributeName);
            if (u != null) {
                System.out.println("ERROR at creating the Unique field!");
                System.out.println("The Unique field " + databaseName + "." + tableName + "." + attributeName + " already exists!");
            } else {
                uniqueKey = t.new UniqueKey(t, attributeName);
            }
        } else {
            System.out.println("ERROR at creating the Unique field!");
            System.out.println("The Table " + databaseName + "." + tableName + " does not exist!\n");
        }
    }

    public void createReference(String databaseName, String tableName, String attributeName, String refTableName, String refAttributeName) {
        Root.Database.Table.ForeignKey f = this.getInstanceOfForeignKey(databaseName, tableName, attributeName);
        if (f != null) {
            Root.Database.Table.ForeignKey.Reference r = getInstanceOfReference(databaseName, tableName, attributeName, refTableName, refAttributeName);
            if (r != null) {
                System.out.println("ERROR at creating the Reference!");
                System.out.println("The Reference " + databaseName + "." + tableName + "." + attributeName + "already exists!");
            } else {
                reference = f.new Reference(f, refTableName, refAttributeName);
            }
        } else {
            System.out.println("ERROR at creating the Reference!");
            System.out.println("The Foreign Key " + databaseName + "." + tableName + "." + attributeName + " does not exist!\n");
        }
    }

    public void deleteReference(String databaseName, String tableName, String attributeName, String refTableName, String refAttributeName) {
        Root.Database.Table.ForeignKey f = getInstanceOfForeignKey(databaseName, tableName, attributeName);
        if (f != null) {
            Root.Database.Table.ForeignKey.Reference r = getInstanceOfReference(databaseName, tableName, attributeName, refTableName, refAttributeName);
            if (r != null) {
                f.references.remove(r);
            } else {
                System.out.println("ERROR at deleting the Reference!");
                System.out.println("The Reference " + databaseName + "." + tableName + "." + attributeName + " does not exist!\n");
            }
        } else {
            System.out.println("ERROR at deleting the Reference!");
            System.out.println("The Foreign Key " + databaseName + "." + tableName + "." + attributeName + " does not exist!\n");
        }
    }

    public List<Root.Database> getInstanceOfDatabases() {
        return root.databases;
    }

    public String getStringOfDatabases() {
        String stringOfDatabeses = "";
        for (Root.Database d : root.databases) {
            stringOfDatabeses += d.databaseName + " ";
        }
        return stringOfDatabeses;
    }

    public Root.Database getInstanceOfDatabase(String databaseName) {
        for (Root.Database database : root.databases) {
            if (Objects.equals(database.databaseName, databaseName)) {
                return database;
            }
        }
        return null;
    }

    private Root.Database.Index getInstanceOfIndex(String databaseName, String indexName) {
        Root.Database d = this.getInstanceOfDatabase(databaseName);
        if (d == null) {
            System.out.println("An error occured while creating the " + indexName + " index in " + databaseName + " database\n");
            return null;
        }
        for (Root.Database.Index i : d.indexes) {
            if (Objects.equals(i.indexName, indexName)) {
                return i;
            }
        }
        return null;
    }

    public String getStringOfTables(String databaseName) {
        String stringOfTables = "";
        Root.Database d = getInstanceOfDatabase(databaseName);
        if (d == null) {
            return "";
        }
        for (Root.Database.Table t : d.tables) {
            stringOfTables += t.tableName + " ";
        }
        if (stringOfTables.equalsIgnoreCase("")) {
            return "";
        }
        return stringOfTables;
    }

    public String getStringOfAttributes(String databaseName, String tableName) {
        Root.Database.Table t = this.getInstanceOfTable(databaseName, tableName);
        if (t == null) {
            return "";
        }
        String stringOfTableFields = "";

        for (Root.Database.Table.Attribute a : t.attributes) {
            stringOfTableFields += a.attrName + " ";
        }

        return stringOfTableFields;
    }

    public String getStringOfForeignKeys(String databaseName, String tableName) {
        Root.Database.Table t = this.getInstanceOfTable(databaseName, tableName);
        return (t == null || t.foreignKeys.isEmpty()) ? "" : t.foreignKeys.stream().map(f -> f.fkName).collect(Collectors.joining(" "));
    }

    public String getStringOfUniqueKeys(String databaseName, String tableName) {
        Root.Database.Table t = this.getInstanceOfTable(databaseName, tableName);
        return (t == null || t.uniqueKeys.isEmpty()) ? "" : t.uniqueKeys.stream().map(u -> u.uqAttrName).collect(Collectors.joining(" "));
    }

    public Root.Database.Table getInstanceOfTable(String databaseName, String tableName) {
        Root.Database d = this.getInstanceOfDatabase(databaseName);
        if (d == null) {
            return null;
        }
        for (Root.Database.Table t : d.tables) {
            if (Objects.equals(t.tableName, tableName)) {
                return t;
            }
        }
        return null;
    }

    public Root.Database.Table.PrimaryKey getInstanceOfPrimaryKey(String databaseName, String tableName, String attributeName) {
        Root.Database.Table t = this.getInstanceOfTable(databaseName, tableName);
        for (Root.Database.Table.PrimaryKey p : t.primaryKeys) {
            if (Objects.equals(p.pkName, attributeName)) {
                return p;
            }
        }
        return null;
    }

    public Root.Database.Table.Attribute getInstanceOfAttribute(String databaseName, String tableName, String attributeName, String attributeType) {
        Root.Database.Table t = this.getInstanceOfTable(databaseName, tableName);
        for (Root.Database.Table.Attribute a : t.attributes) {
            if (Objects.equals(a.attrName, attributeName) && Objects.equals(a.attrType, attributeType)) {
                return a;
            }
        }
        return null;
    }

    public Root.Database.Table.ForeignKey getInstanceOfForeignKey(String databaseName, String tableName, String attributeName) {
        Root.Database.Table t = this.getInstanceOfTable(databaseName, tableName);
        for (Root.Database.Table.ForeignKey f : t.foreignKeys) {
            if (Objects.equals(f.fkName, attributeName)) {
                return f;
            }
        }
        return null;
    }

    public Root.Database.Table.UniqueKey getInstanceOfUnique(String databaseName, String tableName, String attributeName) {
        Root.Database.Table t = this.getInstanceOfTable(databaseName, tableName);
        for (Root.Database.Table.UniqueKey u: t.uniqueKeys) {
            if (Objects.equals(u.uqAttrName, attributeName)) {
                return u;
            }
        }
        return null;
    }

    public Root.Database.Table.ForeignKey.Reference getInstanceOfReference(String databaseName, String tableName, String attributeName, String refTableName, String refAttributeName) {
        Root.Database.Table.ForeignKey f = this.getInstanceOfForeignKey(databaseName, tableName, attributeName);
        for (Root.Database.Table.ForeignKey.Reference r : f.references) {
            if (Objects.equals(r.attrName, refAttributeName) && Objects.equals(r.tableName, refTableName)) {
                return r;
            }
        }
        return null;
    }

    // returns the number of Databases
    public int getNumberOfDatabases() {
        int nrOfDatabases = 0;
        for (Root.Database d : root.databases) {
            nrOfDatabases += 1;
        }
        return nrOfDatabases;
    }

    // returns the number of Tables in a specified Database
    public int getNumberOfTables(String databaseName) {
        Root.Database d = getInstanceOfDatabase(databaseName);
        int nrOfTables = 0;
        if (d != null) {
            for (Root.Database.Table t : d.tables) {
                nrOfTables += 1;
            }
        } else {
            System.out.println("ERROR at getting the number of Tables in a specified Database!");
            System.out.println("The Database " + databaseName + " does not exist!\n");
            return -1;
        }
        return nrOfTables;
    }

    // returns the number of Attributes in a specified Table
    public int getNumberOfAttributes(String databaseName, String tableName) {
        Root.Database.Table t = getInstanceOfTable(databaseName, tableName);
        int nrOfAttributes = 0;
        if (t != null) {
            for (Root.Database.Table.Attribute a : t.attributes) {
                nrOfAttributes += 1;
            }
        } else {
            System.out.println("ERROR at getting the number of Attributes in a specified Table!");
            System.out.println("The Table " + databaseName + "." + tableName + " does not exist!\n");
            return -1;
        }
        return nrOfAttributes;
    }

    // returns the number of Primary keys in a specified Table
    public int getNumberOfPrimaryKeys(String databaseName, String tableName) {
        Root.Database.Table t = getInstanceOfTable(databaseName, tableName);
        int nrOfPrimaryKeys = 0;
        if (t != null) {
            for (Root.Database.Table.PrimaryKey p : t.primaryKeys) {
                nrOfPrimaryKeys += 1;
            }
        } else {
            System.out.println("ERROR at getting the number of Primary Keys in a specified Table!");
            System.out.println("The Table " + databaseName + "." + tableName + " does not exist!\n");
            return -1;
        }
        return nrOfPrimaryKeys;
    }

    // returns the number of Foreign keys in a specified Table
    public int getNumberOfForeignKeys(String databaseName, String tableName) {
        Root.Database.Table t = getInstanceOfTable(databaseName, tableName);
        int nrOfForeignKeys = 0;
        if (t != null) {
            for (Root.Database.Table.ForeignKey p : t.foreignKeys) {
                nrOfForeignKeys += 1;
            }
        } else {
            System.out.println("ERROR at getting the number of Foreign Keys in a specified Table!");
            System.out.println("The Table " + databaseName + "." + tableName + " does not exist!\n");
            return -1;
        }
        return nrOfForeignKeys;
    }

    // returns the number of Unique keys in a specified Table
    public int getNumberOfUniqueKeys(String databaseName, String tableName) {
        Root.Database.Table t = getInstanceOfTable(databaseName, tableName);
        int nrOfUniqueKeys = 0;
        if (t != null) {
            for (Root.Database.Table.UniqueKey u : t.uniqueKeys) {
                nrOfUniqueKeys += 1;
            }
        } else {
            System.out.println("ERROR at getting the number of Unique Keys in a specified Table!");
            System.out.println("The Table " + databaseName + "." + tableName + " does not exist!\n");
            return -1;
        }
        return nrOfUniqueKeys;
    }

    // returns the number of References to a specified Foreign Key
    public int getNumberOfReferences(String databaseName, String tableName, String attributeName) {
        Root.Database.Table.ForeignKey f = getInstanceOfForeignKey(databaseName, tableName, attributeName);
        int nrOfReferences = 0;
        if (f != null) {
            for (Root.Database.Table.ForeignKey.Reference r : f.references) {
                nrOfReferences += 1;
            }
        } else {
            System.out.println("ERROR at getting the number of References to a specified Foreign Key!");
            System.out.println("The Foreign Key " + databaseName + "." + tableName + "." + attributeName + " does not exist!\n");
            return -1;
        }
        return nrOfReferences;
    }

    // returns the index of the given attribute, ex: json file attribute order: id, surname, lastname -> the index of the attribute surname is 1,
    public int getIndexOfAttribute(String databaseName, String tableName, String attributeName) {
        Root.Database.Table t = getInstanceOfTable(databaseName, tableName);
        int indexOfAttribute = 0;
        if (t != null) {
            for (Root.Database.Table.Attribute a : t.attributes) {
                if (Objects.equals(a.attrName, attributeName)) {
                    return indexOfAttribute;
                }
                indexOfAttribute++;
            }
        } else {
            System.out.println("ERROR at getting the index of Attributes in the specified Table!");
            System.out.println("The Table " + databaseName + "." + tableName + " does not exist!\n");
        }
        return -1;
    }

    // return -1 if error, else if attribute type is numeric, returns 0, otherwise returns 1
    public int getAttributeType(String databaseName, String tableName, String attributeName){
        Root.Database.Table t = getInstanceOfTable(databaseName, tableName);
        if (t != null) {
            for (Root.Database.Table.Attribute a : t.attributes) {
                if (Objects.equals(a.attrName, attributeName)) {
                    if (Objects.equals(a.attrType, "INT") || Objects.equals(a.attrType, "FLOAT") || Objects.equals(a.attrType, "BIT")){
                        return 0;
                    }
                    else{
                        return 1;
                    }
                }
            }
        } else {
            System.out.println("ERROR at getting the type of an attribute in the specified table!");
            System.out.println("The Table " + databaseName + "." + tableName + " does not exist!\n");
        }
        return -1;
    }
}
