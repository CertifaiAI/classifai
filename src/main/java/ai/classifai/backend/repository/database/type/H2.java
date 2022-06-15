package ai.classifai.backend.repository.database.type;

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
        driver = "org.h2.Driver";
        dbFileExtension = ".mv.db";
        lckFileExtension = dbFileExtension;
        urlHeader = "jdbc:h2:file:";
        user = "admin";
        password = "";
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
        if(!new File(fileName).exists()) return false;

        FileStore fs = new FileStore();

        try
        {
            fs.open(fileName, true, null);
        }
        catch (IllegalStateException e)
        {
            log.debug(fileName + "cannot be opened.");
            return true;
        }
        finally
        {
            fs.close();
        }

        return false;
    }
}
