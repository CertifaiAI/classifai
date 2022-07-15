/*
 * Copyright (c) 2020-2021 CertifAI Sdn. Bhd.
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
package ai.classifai.core.utility.handler;

import ai.classifai.core.utility.ParamConfig;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.h2.store.fs.FileUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * File Handler
 *
 * @author codenamewei
 */
@Slf4j
public class FileHandler
{
    public static List<String> processFolder(@NonNull File rootPath, @NonNull String[] extensionFormat)
    {
        Predicate<File> func = file -> isFileSupported(file.getAbsolutePath(), extensionFormat);

        return processFolder(rootPath, func);
    }

    public static List<String> processFolder(@NonNull File rootPath, @NonNull Predicate<File> filterFunction)
    {
        List<String> totalFilelist = new ArrayList<>();

        Deque<File> queue = new ArrayDeque<>();

        queue.push(rootPath);

        while (!queue.isEmpty())
        {
            File currentFolderPath = queue.pop();

            List<File> folderList = listFiles(currentFolderPath);

            for (File file : folderList)
            {
                if (file.isDirectory())
                {
                    queue.push(file);
                }
                else
                {
                    if (filterFunction.test(file))
                    {
                        totalFilelist.add(file.getAbsolutePath());
                    }
                }
            }
        }

        return totalFilelist;
    }

    private static FilenameFilter getDeletedImageFolderFilter()
    {
        // Return false if name is equal to delete folder name, true otherwise
        return (dir, name) -> !name.equals(ParamConfig.getDeleteDataFolderName());
    }

    private static List<File> listFiles(File rootPath)
    {
        List<File> outputList = new ArrayList<>();

        if (rootPath.exists())
        {
            outputList = Arrays.stream(Objects.requireNonNull(rootPath.listFiles(getDeletedImageFolderFilter())))
                    .collect(Collectors.toList());
        }

        return outputList;
    }

    public static boolean isFileSupported(String file, String[] formatTypes)
    {
        for (String format : formatTypes)
        {
            Integer beginIndex = file.length() - format.length();
            Integer endIndex = file.length();

            if (file.substring(beginIndex, endIndex).equals(format))
            {
                return true;
            }
        }
        return false;
    }

    public static String trimPath(String rootPath, String fullPath)
    {
        return fullPath.substring(rootPath.length());
    }


    public static String getFileName(@NonNull String filePath)
    {
        String fileNameWithExtension = new File(filePath).getName();

        String[] separator = fileNameWithExtension.split("\\.");

        int fileEndIndex = filePath.length() -  separator[(separator.length - 1)].length() - 1;
        int fileStartIndex = filePath.length() - fileNameWithExtension.length();

        String fileName = filePath.substring(fileStartIndex, fileEndIndex);

        return fileName;
    }

    private static void delete(File file)
    {
        try
        {
            Files.delete(file.toPath());
        }
        catch (Exception e)
        {
            log.debug("unable to delete" + file.getName());
        }
    }

    public static boolean deleteFile(File file)
    {
        try
        {
            //folder
            if (file.isDirectory())
            {
                File[] allContents = file.listFiles();
                if (allContents != null)
                {
                    for (File buffer : allContents)
                    {
                        deleteFile(buffer);
                    }
                }

                delete(file);
            }
            //file
            else
            {
                delete(file);
            }
            return true;
        }
        catch (Exception e)
        {
            log.debug("Unable to delete " + file.getName());
            return false;
        }
    }

    public static boolean createFolderIfNotExist(File file)
    {
        if(!file.exists() && !file.mkdir())
        {
            log.debug("Failed to create " + file.getAbsolutePath());
            return false;
        }
        return true;
    }

    public static String createProjectFolder(File projectFile) throws IOException {
        String projectDirectory = projectFile.getParent() + File.separator + FilenameUtils.getBaseName(projectFile.getName()) + "_classifai";
        File projectFolderPath = new File(projectDirectory);

        if (!projectFolderPath.exists()) {
            Files.createDirectory(Paths.get(projectDirectory));
            log.info("Project directory is created at " + projectDirectory);
        }

        return projectDirectory;
    }

    public static String moveFileToProjectFolder(String projectFolderPath, String projectFilePath) throws IOException {
        File projectFile = new File(projectFilePath);
        String targetProjectFilePath = projectFolderPath + File.separator + projectFile.getName();
        Files.move(Paths.get(projectFile.getAbsolutePath()), Paths.get(targetProjectFilePath));
        log.info(projectFile.getName() + " has move to directory: " + projectFolderPath);

        return targetProjectFilePath;
    }

}
