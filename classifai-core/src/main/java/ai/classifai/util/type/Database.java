package ai.classifai.util.type;

public enum Database {
    HSQL(".lck", ".script","org.hsqldb.jdbcDriver", "jdbc:hsqldb:file:",null,null),
    H2(".lock.db",".mv.db", "org.h2.Driver", "jdbc:h2:file:", "admin", "" );

    private final String LCK_FILE_EXTENSION;
    private final String DB_FILE_EXTENSION;
    private final String DRIVER;
    private final String URL_HEADER;
    private final String USER;
    private final String PASSWORD;
    Database(final String LCK_FILE_EXTENSION, final String DB_FILE_EXTENSION, final String DRIVER, final String URL_HEADER, final String USER, final String PASSWORD ){
        this.LCK_FILE_EXTENSION = LCK_FILE_EXTENSION;
        this.DB_FILE_EXTENSION = DB_FILE_EXTENSION;
        this.DRIVER = DRIVER;
        this.URL_HEADER = URL_HEADER;
        this.USER = USER;
        this.PASSWORD = PASSWORD;

    }

    public String getDB_FILE_EXTENSION() {
        return DB_FILE_EXTENSION;
    }

    public String getLCK_FILE_EXTENSION() {
        return LCK_FILE_EXTENSION;
    }

    public String getDRIVER() { return DRIVER; }

    public String getURL_HEADER() { return URL_HEADER; }

    public String getUSER() { return USER; }

    public String getPASSWORD() { return PASSWORD; }
}
