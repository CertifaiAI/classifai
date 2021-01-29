package ai.classifai.util;

import ai.classifai.database.config.DatabaseConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;

/***
 *Archive Handler
 *
 *@author YCCertifai
 */
@Slf4j
public class ArchiveHandler {
    private static final String ARCHIVE_PATH;

    static{
        ARCHIVE_PATH = DatabaseConfig.getRootPath() + File.separator + ".archive";
        createArchiveFolder();
    }

    public static String getArchivePath(){ return ARCHIVE_PATH;}

    private static void createArchiveFolder(){
        File file = new File(ARCHIVE_PATH);
        if(! file.exists()) file.mkdir();
    }

    public static void moveToArchive(String path) {
        try {
            File source = new File(path);
            File destination = new File(ARCHIVE_PATH,source.getName());
            if( source.isDirectory()) {
                FileUtils.moveDirectory(source, destination);
            }
            else{
                FileUtils.moveFile(source, destination);
            }
        }
        catch( Exception e){
            log.debug("unable to move " + ARCHIVE_PATH + e);
        }
    }

}
