package ai.classifai.database;

import ai.classifai.util.type.Database;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

/**
 * Common configurations for files and paths of database
 *
 * @author codenamewei
 */
@Slf4j
public class DatabaseConfig {
    private static final String ROOT_PATH;

    private static final String PORTFOLIO_DB_NAME;
    private static final String BNDBOX_DB_NAME;
    private static final String SEG_DB_NAME;

    private static final String PORTFOLIO_DB_PATH;
    private static final String BNDBOX_DB_PATH;
    private static final String SEG_DB_PATH;

    private final static String PORTFOLIO_DIR_PATH;
    private final static String BNDBOX_DIR_PATH;
    private final static String SEG_DIR_PATH;

    private final File PORTFOLIO_DB_DBFILE;
    private final File BNDBOX_DB_DBFILE;
    private final File SEG_DB_DBFILE;

    private final File PORTFOLIO_DB_LCKPATH;
    private final File BNDBOX_DB_LCKPATH;
    private final File SEG_DB_LCKPATH;

    static {
        ROOT_PATH = System.getProperty("user.home") + File.separator + ".classifai";

        PORTFOLIO_DB_NAME = "portfolio";
        BNDBOX_DB_NAME = "bbproject";
        SEG_DB_NAME = "segproject";

        PORTFOLIO_DIR_PATH = defineDirPath(PORTFOLIO_DB_NAME);
        BNDBOX_DIR_PATH = defineDirPath(BNDBOX_DB_NAME);
        SEG_DIR_PATH = defineDirPath(SEG_DB_NAME);

        PORTFOLIO_DB_PATH = defineDbPath(PORTFOLIO_DB_NAME);
        BNDBOX_DB_PATH = defineDbPath(BNDBOX_DB_NAME);
        SEG_DB_PATH = defineDbPath(SEG_DB_NAME);
    }

    public DatabaseConfig(Database database){
        String LCK_FILE_EXTENSION = database.getLCK_FILE_EXTENSION();
        String DB_FILE_EXTENSION = database.getDB_FILE_EXTENSION();

        PORTFOLIO_DB_LCKPATH = new File(PORTFOLIO_DB_PATH + LCK_FILE_EXTENSION);
        BNDBOX_DB_LCKPATH = new File(BNDBOX_DB_PATH + LCK_FILE_EXTENSION);
        SEG_DB_LCKPATH = new File(SEG_DB_PATH + LCK_FILE_EXTENSION);

        PORTFOLIO_DB_DBFILE = new File(PORTFOLIO_DB_PATH + DB_FILE_EXTENSION);
        BNDBOX_DB_DBFILE = new File(BNDBOX_DB_PATH + DB_FILE_EXTENSION);
        SEG_DB_DBFILE = new File(SEG_DB_PATH + DB_FILE_EXTENSION);
    }

    private static String defineDirPath(String database) {
        return ROOT_PATH + File.separator + database;
    }

    private static String defineDbPath(String database) {
        return defineDirPath(database) + File.separator + database + "db";
    }

    public static String getRootPath() {
        return ROOT_PATH;
    }

    public static String getPortfolioDbPath() {
        return PORTFOLIO_DB_PATH;
    }

    public static String getBndboxDbPath() {
        return BNDBOX_DB_PATH;
    }

    public static String getSegDbPath() {
        return SEG_DB_PATH;
    }

    public static String getPortfolioDirPath() {
        return PORTFOLIO_DIR_PATH;
    }

    public static String getBndboxDirPath() {
        return BNDBOX_DIR_PATH;
    }

    public static String getSegDirPath() {
        return SEG_DIR_PATH;
    }

    public File getPortfolioLockPath() { return PORTFOLIO_DB_LCKPATH; }

    public File getBndBoxLockPath() { return BNDBOX_DB_LCKPATH; }

    public File getSegLockPath() { return SEG_DB_LCKPATH; }

    public String getPortfolioDbFileName(){ return PORTFOLIO_DB_DBFILE.getName(); }

    public String getBndboxDbFileName(){ return BNDBOX_DB_DBFILE.getName(); }

    public String getSegDbFileName(){ return SEG_DB_DBFILE.getName(); }

    public void deleteLckFile()
    {
        try
        {
            if( PORTFOLIO_DB_LCKPATH.exists() && ! PORTFOLIO_DB_LCKPATH.delete())
            {
                log.debug("Delete portfolio lock file failed from path: " + PORTFOLIO_DB_LCKPATH.getAbsolutePath());
            }

            if( BNDBOX_DB_LCKPATH.exists() && ! BNDBOX_DB_LCKPATH.delete())
            {
                log.debug("Delete boundingbox lock file failed from path: " + BNDBOX_DB_LCKPATH.getAbsolutePath());
            }

            if( SEG_DB_LCKPATH.exists() && ! SEG_DB_LCKPATH.delete())
            {
                log.debug("Delete segmentation lock file failed from path: " + SEG_DB_LCKPATH.getAbsolutePath());
            }
        }
        catch(Exception e) {
            log.debug("Error when delete lock file: ", e);
        }
    }

    public boolean isDatabaseExist()
    {
        return (PORTFOLIO_DB_DBFILE.exists() && BNDBOX_DB_DBFILE.exists() && SEG_DB_DBFILE.exists());
    }

    public boolean isDatabaseSetup(boolean unlockDatabase)
    {
        if(unlockDatabase)
        {
            deleteLckFile();
        }
        else
        {
            return isDbReadyForAccess();
        }

        return true;
    }

    private boolean isDbReadyForAccess()
    {
        if(PORTFOLIO_DB_LCKPATH.exists() || BNDBOX_DB_LCKPATH.exists() || SEG_DB_LCKPATH.exists())
        {
            log.info("Database is locked. Try with --unlockdb. \n" +
                    "WARNING: This might be hazardous by allowing multiple access to the database.");

            return false;
        }

        return true;
    }

}
