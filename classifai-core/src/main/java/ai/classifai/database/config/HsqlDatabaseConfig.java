package ai.classifai.database.config;

import lombok.extern.slf4j.Slf4j;

import java.io.File;

/**
 *  Configurations for files and paths of database for HSQL Database
 *
 * @author YCCertifai
 */
@Slf4j
public class HsqlDatabaseConfig extends DatabaseConfig {

    static {
        LCK_FILE_EXTENSION = ".lck";
        DB_FILE_EXTENSION = ".script";

        PORTFOLIO_DB_LCKPATH = new File(PORTFOLIO_DB_PATH + LCK_FILE_EXTENSION);
        BNDBOX_DB_LCKPATH = new File(BNDBOX_DB_PATH + LCK_FILE_EXTENSION);
        SEG_DB_LCKPATH = new File(SEG_DB_PATH + LCK_FILE_EXTENSION);

        PORTFOLIO_DB_DBFILE = new File(PORTFOLIO_DB_PATH + DB_FILE_EXTENSION);
        BNDBOX_DB_DBFILE = new File(BNDBOX_DB_PATH + DB_FILE_EXTENSION);
        SEG_DB_DBFILE = new File(SEG_DB_PATH + DB_FILE_EXTENSION);
    }

    public static boolean isDatabaseExist()
    {
        return (PORTFOLIO_DB_DBFILE.exists() && BNDBOX_DB_DBFILE.exists() && SEG_DB_DBFILE.exists());
    }
}
