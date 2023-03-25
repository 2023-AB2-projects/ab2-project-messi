/*
    (The Server overrides these methods)
    Return value:
        - error:  -1
        - success: 1
 */
public interface Handler {
    int createDatabase(String dbName);
    int createTable(String dbName, String tbName,  String pKey, String fKey, String uqKey, String fieldNames, String fieldTypes);
    int dropDatabase(String dbName);
    int dropTable(String dbName, String tbName);
    int createIndex(String dbName, String tbName, String indexName, String fieldName);
}