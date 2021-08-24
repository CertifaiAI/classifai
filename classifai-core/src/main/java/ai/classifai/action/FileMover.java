/*
 * Copyright (c) 2021 CertifAI Sdn. Bhd.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package ai.classifai.action;

import ai.classifai.util.ParamConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Utility class for moving files
 *
 * @author devenyantis
 */
@Slf4j
public class FileMover {

    private FileMover() {
        throw new IllegalStateException("Utility class");
    }

    public static void moveFileToDirectory(String oldDir, List<String> filesToMove) throws IOException {

        String newDirStr = createDeletedDataFolder(oldDir, ParamConfig.getDeleteDataFolderName());
        File newDir = Paths.get(newDirStr).toFile();

        if(!newDir.exists() && !newDir.mkdir()) {
            log.info("Fail to create directory " + newDirStr);
            return;
        }

        moveFiles(newDirStr, filesToMove);
    }

    public static String createDeletedDataFolder(String oldDir, String newDirName) {
        String folderName = Paths.get(oldDir, newDirName).toString();
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

    public static void moveConfigFile(File folderName, Path source, Path target) {

        if(!folderName.exists()) {
            folderName.mkdir();
            log.info("Backup folder " + folderName.getName() + " is created");
        }

        try {
            Files.move(source, target);
        } catch (IOException e) {
            log.info("Configuration file fail to move to backup folder");
        }

        log.info("Please export a new project configuration file for this project");
    }
}
