package ai.classifai.action;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;

@Slf4j
public class FileMover {

    public static void moveFileToDirectory(String oldDir, List<String> filesToMove) throws IOException {

        String newDirStr = createNewDirFromOld(oldDir, "deleted");
        File newDir = Paths.get(newDirStr).toFile();

        if(!newDir.exists()) {
            if(!newDir.mkdir()) {
                log.info("Fail to create directory " + newDirStr);
                return;
            }
        }

        moveFiles(newDirStr, filesToMove);
    }

    public static String createNewDirFromOld(String oldDir, String newDirIdentifier) {
        String folderName = oldDir + "_" + newDirIdentifier;
        log.debug("Creating new dir with identifier: " + folderName);
        return folderName;
    }

    private static void moveFiles(String newDirStr, List<String> filesToMove) throws IOException {
        for(String srcFile: filesToMove) {
            String fName = Paths.get(srcFile).getFileName().toString();
            log.info("DEVEN: \n" + Paths.get(srcFile) + "\n" + Paths.get(newDirStr, fName));
            Files.move(
                    Paths.get(srcFile),
                    Paths.get(newDirStr, fName));
        }
    }
}
