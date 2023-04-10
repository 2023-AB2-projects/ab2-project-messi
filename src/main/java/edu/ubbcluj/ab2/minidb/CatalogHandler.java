package edu.ubbcluj.ab2.minidb;

import com.mongodb.client.MongoClient;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

public class CatalogHandler {
    private Root root;
    private Root.Database database;
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
            System.out.println("ERROR at writing to " + fileName + " file!");
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
        database = root.new Database(root, databaseName);
    }

    public void dropDatabase(String databaseName) {
        Root.Database d = getInstanceOfDatabase(databaseName);
        root.databases.remove(d);
    }

    public void createTable(String databaseName, String tableName) {
        Root.Database d = this.getInstanceOfDatabase(databaseName);
        table = d.new Table(d, tableName);
    }

    public void dropTable(String databaseName, String tableName) {
        Root.Database.Table t = this.getInstanceOfTable(databaseName, tableName);
        Root.Database d = this.getInstanceOfDatabase(databaseName);
        d.tables.remove(t);
    }

    public void createAttribute(String databaseName, String tableName, String attributeName, String attributeType) {
        Root.Database.Table t = this.getInstanceOfTable(databaseName, tableName);
        if (t != null) {
            attribute = t.new Attribute(t, attributeName, attributeType);
        }
    }

    public void deleteAttribute(String databaseName, String tableName, String attributeName, String attributeType) {
        Root.Database.Table.Attribute a = getInstanceOfAttribute(databaseName, tableName, attributeName, attributeType);
        table.attributes.remove(a);
    }

    public void createPrimaryKey(String databaseName, String tableName, String attributeName, String attributeType) {
        Root.Database.Table t = this.getInstanceOfTable(databaseName, tableName);
        if (t != null) {
            primaryKey = t.new PrimaryKey(t, attributeName, attributeType);
        }
    }

    public void deletePrimaryKey(String databaseName, String tableName, String attributeName, String attributeType) {
        Root.Database.Table.PrimaryKey p = getInstanceOfPrimaryKey(databaseName, tableName, attributeName, attributeType);
        table.primaryKeys.remove(p);
    }

    public void createForeignKey(String databaseName, String tableName, String attributeName) {
        Root.Database.Table t = this.getInstanceOfTable(databaseName, tableName);
        if (t != null) {
            foreignKey = t.new ForeignKey(t, attributeName);
        }
    }

    public void deleteForeignKey(String databaseName, String tableName, String attributeName) {
        Root.Database.Table.ForeignKey f = getInstanceOfForeignKey(databaseName, tableName, attributeName);
        table.foreignKeys.remove(f);
    }

    public void createReference(String databaseName, String tableName, String attributeName, String refTableName, String refAttributeName) {
        Root.Database.Table.ForeignKey f = this.getInstanceOfForeignKey(databaseName, tableName, attributeName);
        if (f != null) {
            reference = f.new Reference(f, refTableName, refAttributeName);
        }
    }

    public void deleteReference(String databaseName, String tableName, String attributeName, String refTableName, String refAttributeName) {
        Root.Database.Table.ForeignKey.Reference r = getInstanceOfReference(databaseName, tableName, attributeName, refTableName, refAttributeName);
        foreignKey.references.remove(r);
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

    public Root.Database.Table getInstanceOfTable(String databaseName, String tableName) {
        Root.Database d = this.getInstanceOfDatabase(databaseName);
        for (Root.Database.Table t : d.tables) {
            if (Objects.equals(t.tableName, tableName)) {
                return t;
            }
        }
        return null;
    }

    public Root.Database.Table.PrimaryKey getInstanceOfPrimaryKey(String databaseName, String tableName, String attributeName, String attributeType) {
        Root.Database.Table t = this.getInstanceOfTable(databaseName, tableName);
        for (Root.Database.Table.PrimaryKey p : t.primaryKeys) {
            if (Objects.equals(p.pkName, attributeName) && Objects.equals(p.pkType, attributeType)) {
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

    public Root.Database.Table.ForeignKey.Reference getInstanceOfReference(String databaseName, String tableName, String attributeName, String refTableName, String refAttributeName) {
        Root.Database.Table.ForeignKey f = this.getInstanceOfForeignKey(databaseName, tableName, attributeName);
        for (Root.Database.Table.ForeignKey.Reference r : f.references) {
            if (Objects.equals(r.attrName, refAttributeName) && Objects.equals(r.tableName, refTableName)) {
                return r;
            }
        }
        return null;
    }
}
