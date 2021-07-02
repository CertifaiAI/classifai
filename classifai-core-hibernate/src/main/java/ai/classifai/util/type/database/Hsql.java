package ai.classifai.util.type.database;

import ai.classifai.util.data.FileHandler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;

/***
 * V1 Database
 *
 * @author codenamewei
 */
@Getter
@Slf4j
public class Hsql extends RelationalDb
{
    public Hsql()
    {
        driver = "org.hsqldb.jdbcDriver";
        dbFileExtension = ".script";
        lckFileExtension = ".lck";
        urlHeader = "jdbc:hsqldb:file:";
        user = null;
        password = null;
        tableAbsPathDict = null;
        lockAbsPathList = null;
    }

//    public boolean removeLckIfExist()
//    {
//        for(File filePath : lockAbsPathList)
//        {
//            if (filePath.exists() && !FileHandler.deleteFile(filePath)){
//
//                return false;
//            }
//        }
//
//        return true;
//    }

}
