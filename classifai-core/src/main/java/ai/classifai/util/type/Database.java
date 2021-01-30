package ai.classifai.util.type;

public enum Database {
    HSQL(".lck", ".script"),
    H2(".lock.db",".mv.db");

    private final String LCK_FILE_EXTENSION;
    private final String DB_FILE_EXTENSION;
    Database(final String LCK_FILE_EXTENSION, final String DB_FILE_EXTENSION){
        this.LCK_FILE_EXTENSION = LCK_FILE_EXTENSION;
        this.DB_FILE_EXTENSION = DB_FILE_EXTENSION;
    }

    public String getDB_FILE_EXTENSION() {
        return DB_FILE_EXTENSION;
    }

    public String getLCK_FILE_EXTENSION() {
        return LCK_FILE_EXTENSION;
    }
}
