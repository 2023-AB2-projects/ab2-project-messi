package edu.ubbcluj.ab2.minidb;

import org.json.JSONArray;
import org.json.JSONObject;

public class RootDeserializer {
    public static Root deserialize(String jsonString) {
        Root root = new Root();
        JSONObject rootObject = new JSONObject(jsonString);
        JSONArray databaseArray = (JSONArray) rootObject.get("Databases");

        for (Object databaseObject : databaseArray) {
            JSONObject databaseJson = (JSONObject) databaseObject;
            String databaseName = (String) databaseJson.get("Database");
            Root.Database database = root.new Database(root, databaseName);
            JSONArray tableArray = (JSONArray) databaseJson.get("Tables");

            for (Object tableObject : tableArray) {
                JSONObject tableJson = (JSONObject) tableObject;
                String tableName = (String) tableJson.get("Table");
                Root.Database.Table table = database.new Table(database, tableName);

                JSONArray attributeArray = (JSONArray) tableJson.get("Attributes");
                for (Object attributeObject : attributeArray) {
                    JSONObject attributeJson = (JSONObject) attributeObject;
                    String attrName = (String) attributeJson.get("attrName");
                    String attrType = (String) attributeJson.get("attrType");
                    Root.Database.Table.Attribute attribute = table.new Attribute(table, attrName, attrType);
                }

                JSONArray primaryKeyArray = (JSONArray) tableJson.get("PrimaryKeys");
                for (Object primaryKeyObject : primaryKeyArray) {
                    JSONObject primaryKeyJson = (JSONObject) primaryKeyObject;
                    String pkName = (String) primaryKeyJson.get("pkName");
                    String pkType = (String) primaryKeyJson.get("pkType");
                    Root.Database.Table.PrimaryKey primaryKey = table.new PrimaryKey(table, pkName, pkType);
                }

                JSONArray foreignKeyArray = (JSONArray) tableJson.get("ForeignKeys");
                for (Object foreignKeyObject : foreignKeyArray) {
                    JSONObject foreignKeyJson = (JSONObject) foreignKeyObject;
                    String fkName = (String) foreignKeyJson.get("fkName");
                    Root.Database.Table.ForeignKey foreignKey = table.new ForeignKey(table, fkName);

                    JSONArray referenceArray = (JSONArray) foreignKeyJson.get("References");
                    for (Object referenceObject : referenceArray) {
                        JSONObject referenceJson = (JSONObject) referenceObject;
                        String refTableName = (String) referenceJson.get("tableName");
                        String refAttrName = (String) referenceJson.get("attrName");
                        Root.Database.Table.ForeignKey.Reference reference = foreignKey.new Reference(foreignKey, refTableName, refAttrName);
                    }
                }

                JSONArray uniqueKeyArray = (JSONArray) tableJson.get("UniqueKeys");
                for (Object uniqueKeyObject : uniqueKeyArray) {
                    JSONObject uniqueKeyJson = (JSONObject) uniqueKeyObject;
                    String uqAttrName = (String) uniqueKeyJson.get("uqAttrName");
                    Root.Database.Table.UniqueKey uniqueKey = table.new UniqueKey(table, uqAttrName);
                }
            }
        }

        return root;
    }
}