import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Database {
    private List<Table> tables;
    private String databaseName;

    public class Table {
        private String tableName;
        private List<Attribute> attributes;
        private List<PrimaryKey> primaryKeys;
        private List<ForeignKey> foreignKeys;
        private List<UniqueKey> uniqueKeys;

        public class Attribute {
            private String attrName;
            private String attrType;

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

        public class PrimaryKey {
            private String pkName;
            private String pkType;

            public PrimaryKey(Table table, String pkName, String pkType) {
                this.pkName = pkName;
                this.pkType = pkType;
                table.primaryKeys.add(this);
            }

            public JSONObject toJsonObject() {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("pkName", pkName);
                jsonObject.put("pkType", pkType);
                return jsonObject;
            }
        }

        public class ForeignKey {
            private String fkName;
            private List<Reference> references;

            public class Reference {
                private String tableName;
                private String attrName;

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
                    jsonArray.add(reference.toJsonObject());
                }
                jsonObject.put("References", jsonArray);

                return jsonObject;
            }
        }

        public class UniqueKey{
            private String uqAttrName;

            public UniqueKey(Table table, String uqAttrName){
                this.uqAttrName = uqAttrName;
                table.uniqueKeys.add(this);
            }

            public JSONObject toJsonObject() {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("uqAttrName", uqAttrName);
                return jsonObject;
            }
        }

        // indexfile

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
                jsonArray1.add(attribute.toJsonObject());
            }
            jsonObject.put("Attributes", jsonArray1);

            jsonObject.put("Attributes", jsonArray1);
            JSONArray jsonArray2 = new JSONArray();
            for (PrimaryKey primaryKey : primaryKeys) {
                jsonArray2.add(primaryKey.toJsonObject());
            }
            jsonObject.put("PrimaryKeys", jsonArray2);

            JSONArray jsonArray3 = new JSONArray();
            for (ForeignKey foreignKey : foreignKeys) {
                jsonArray3.add(foreignKey.toJsonObject());
            }
            jsonObject.put("ForeignKeys", jsonArray3);

            JSONArray jsonArray4 = new JSONArray();
            for (UniqueKey uniqueKey : uniqueKeys) {
                jsonArray4.add(uniqueKey.toJsonObject());
            }
            jsonObject.put("UniqueKeys", jsonArray4);

            return jsonObject;
        }
    }

    public Database(String databaseName) {
        this.databaseName = databaseName;
        tables = new ArrayList<>();
    }

    public JSONObject toJsonObject() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("Database", databaseName);
        JSONArray jsonArray = new JSONArray();
        for (Table table : tables) {
            jsonArray.add(table.toJsonObject());
        }
        jsonObject.put("Tables", jsonArray);

        return jsonObject;
    }
}
