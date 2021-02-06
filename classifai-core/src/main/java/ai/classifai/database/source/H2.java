package ai.classifai.database.source;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.h2.mvstore.FileStore;

import java.io.File;

/***
 * V2 Database
 *
 * @author codenamewei
 */
@Getter
@Slf4j
public class H2 extends RelationalDb
{
    public H2()
    {
        driver = "org.h2.jdbcDriver";
        dbFileExtension = ".mv.db";
        lckFileExtension = dbFileExtension;
        urlHeader = "jdbc:h2:file:";
        user = "admin";
        password = "admin";
        tableAbsPathDict = null;
        lockAbsPathList = null;

    }

    public boolean isDbLocked()
    {
        if(lockAbsPathList == null)
        {
            log.debug("Db lock path is not set. Failed to proceed checking if locked");
            return true;
        }

        for(File lockPath : lockAbsPathList)
        {
            if(isFileLocked(lockPath.getAbsolutePath()))
            {
                //any exist will return true
                return true;
            }
        }

        return false;
    }

    private boolean isFileLocked(String fileName)
    {
        FileStore fs = new FileStore();

        try
        {
            fs.open(fileName, true, null);
            return false;
        }
        catch (IllegalStateException e)
        {
            log.debug("Failed in opening locked file:", fileName);
            return true;
        }
        finally
        {
            fs.close();
        }
    }
}
