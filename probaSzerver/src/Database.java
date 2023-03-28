public class Database {
    private Table[] tables;
    private String databaseName;

    public class Table {
        private Attribute[] attributes;
        private PrimaryKey primaryKey;
        private ForeignKey[] foreignKeys;
        private UniqueKey[] uniqueKeys;

        public class Attribute {
            private String attrName;
            private String attrType;
        }

        public class PrimaryKey {
            private String pkName;
            private String pkType;
        }

        public class ForeignKey {
            private String fkName;
            private Reference[] references;

            public class Reference {
                private String tableName;
                private String attrName;
            }
        }

        public class UniqueKey{
            private String uqAttrName;
        }

        // indexfile
    }
}
