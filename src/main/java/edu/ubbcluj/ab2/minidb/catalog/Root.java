package edu.ubbcluj.ab2.minidb;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

//@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class Root {
    public List<Database> databases;

    public Root() {
        databases = new ArrayList<>();
    }

    public JSONObject toJsonObject() {
        JSONObject jsonObject = new JSONObject();

        JSONArray jsonArray = new JSONArray();
        for (Database database : databases) {
            jsonArray.put(database.toJsonObject());
        }
        jsonObject.put("Databases", jsonArray);

        return jsonObject;
    }

    //    @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
    public class Database {
        public String databaseName;
        public List<Index> indexes;
        public List<Table> tables;

        public Database(Root root, String databaseName) {
            this.databaseName = databaseName;
            this.indexes = new ArrayList<>();
            this.tables = new ArrayList<>();
            root.databases.add(this);
        }

        public JSONObject toJsonObject() {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("Database", databaseName);

            JSONArray jsonArray1 = new JSONArray();
            for (Index index : indexes) {
                jsonArray1.put(index.toJsonObject());
            }
            jsonObject.put("Indexes", jsonArray1);

            JSONArray jsonArray2 = new JSONArray();
            for (Table table : tables) {
                jsonArray2.put(table.toJsonObject());
            }
            jsonObject.put("Tables", jsonArray2);

            return jsonObject;
        }

        public class Index {
            public String  indexName;
            public String[] fields;

            public Index(Database database, String indexName, String[] fields) {
                this.indexName = indexName;
                this.fields = fields;
                database.indexes.add(this);
            }

            public JSONObject toJsonObject() {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("indexName", indexName);

                JSONArray jsonArray = new JSONArray();
                for (String field: fields) {
                    jsonArray.put(field);
                }
                jsonObject.put("fields", jsonArray);

                return jsonObject;
            }
        }

        //        @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
        public class Table {
            public String tableName;
            public List<Attribute> attributes;
            public List<PrimaryKey> primaryKeys;
            public List<ForeignKey> foreignKeys;
            public List<UniqueKey> uniqueKeys;

            public Table(Database database, String tableName) {
                this.tableName = tableName;
                attributes = new ArrayList<>();
                primaryKeys = new ArrayList<>();
                foreignKeys = new ArrayList<>();
                uniqueKeys = new ArrayList<>();
                database.tables.add(this);
            }

            public JSONObject toJsonObject() {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("Table", tableName);

                JSONArray jsonArray1 = new JSONArray();
                for (Attribute attribute : attributes) {
                    jsonArray1.put(attribute.toJsonObject());
                }
                jsonObject.put("Attributes", jsonArray1);

                jsonObject.put("Attributes", jsonArray1);
                JSONArray jsonArray2 = new JSONArray();
                for (PrimaryKey primaryKey : primaryKeys) {
                    jsonArray2.put(primaryKey.toJsonObject());
                }
                jsonObject.put("PrimaryKeys", jsonArray2);

                JSONArray jsonArray3 = new JSONArray();
                for (ForeignKey foreignKey : foreignKeys) {
                    jsonArray3.put(foreignKey.toJsonObject());
                }
                jsonObject.put("ForeignKeys", jsonArray3);

                JSONArray jsonArray4 = new JSONArray();
                for (UniqueKey uniqueKey : uniqueKeys) {
                    jsonArray4.put(uniqueKey.toJsonObject());
                }
                jsonObject.put("UniqueKeys", jsonArray4);

                return jsonObject;
            }

            //            @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
            public class Attribute {
                public String attrName;
                public String attrType;

                public Attribute(Table table, String attrName, String attrType) {
                    this.attrName = attrName;
                    this.attrType = attrType;
                    table.attributes.add(this);
                }

                public JSONObject toJsonObject() {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("attrName", attrName);
                    jsonObject.put("attrType", attrType);
                    return jsonObject;
                }
            }

            //            @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
            public class PrimaryKey {
                public String pkName;

                public PrimaryKey(Table table, String pkName) {
                    this.pkName = pkName;
                    table.primaryKeys.add(this);
                }

                public JSONObject toJsonObject() {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("pkName", pkName);
                    return jsonObject;
                }
            }

            // indexfile

            //            @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
            public class ForeignKey {
                public String fkName;
                public List<Reference> references;

                public ForeignKey(Table table, String fkName) {
                    this.fkName = fkName;
                    this.references = new ArrayList<>();
                    table.foreignKeys.add(this);
                }

                public JSONObject toJsonObject() {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("fkName", fkName);

                    JSONArray jsonArray = new JSONArray();
                    for (Reference reference : references) {
                        jsonArray.put(reference.toJsonObject());
                    }
                    jsonObject.put("References", jsonArray);

                    return jsonObject;
                }

                //                @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
                public class Reference {
                    public String tableName;
                    public String attrName;

                    public Reference(ForeignKey foreignKey, String tableName, String attrName) {
                        this.tableName = tableName;
                        this.attrName = attrName;
                        foreignKey.references.add(this);
                    }

                    public JSONObject toJsonObject() {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("tableName", tableName);
                        jsonObject.put("attrName", attrName);
                        return jsonObject;
                    }
                }
            }

            //            @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
            public class UniqueKey {
                public String uqAttrName;

                public UniqueKey(Table table, String uqAttrName) {
                    this.uqAttrName = uqAttrName;
                    table.uniqueKeys.add(this);
                }

                public JSONObject toJsonObject() {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("uqAttrName", uqAttrName);
                    return jsonObject;
                }
            }
        }
    }
}