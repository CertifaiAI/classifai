package ai.classifai.action;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
            Path src = Paths.get(srcFile);
            Path des = Paths.get(newDirStr, fName);

            // Check modify filename if exist
            if(des.toFile().exists()) {
                des = getUniqueFilename(newDirStr, des);
            }

            log.debug("Move deleted file:\n" + "From: " + src + "\nTo: " + des);
            Files.move(src, des);
        }
    }

    private static Path getUniqueFilename(String newDirStr, Path des)
    {
        int num = 0;

        File desFile = des.toFile();
        while(desFile.exists()) {
            String baseName = FilenameUtils.getBaseName(desFile.toString());
            String ext = FilenameUtils.getExtension(desFile.toString());
            num++;
            desFile = Paths.get(newDirStr,baseName + "(" + num + ")" + "." + ext).toFile();
        }

        return desFile.toPath();
    }
}
