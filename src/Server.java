public class Server implements Handler{


    @Override
    public int createDatabase(String dbName) {
        return 0;
    }

    @Override
    public int createTable(String dbName, String tbName, String pKey, String fKey, String uqKey, String fieldNames, String fieldTypes) {
        return 0;
    }

    @Override
    public int dropDatabase(String dbName) {
        return 0;
    }

    @Override
    public int dropTable(String dbName, String tbName) {
        return 0;
    }

    @Override
    public int createIndex(String dbName, String tbName, String indexName, String fieldName) {
        return 0;
    }
}
