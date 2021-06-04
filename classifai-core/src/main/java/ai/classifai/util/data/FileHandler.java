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
package ai.classifai.util.data;

import ai.classifai.util.ParamConfig;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * File Handler
 *
 * @author codenamewei
 */
@Slf4j
public class FileHandler
{
    public static List<File> processFolder(@NonNull File rootPath, @NonNull String[] extensionFormat)
    {
        Function<File, Boolean> func = file -> isFileSupported(file.getAbsolutePath(), extensionFormat);

        return processFolder(rootPath, func);
    }

    public static List<File> processFolder(@NonNull File rootPath, @NonNull Function<File, Boolean> filterFunction)
    {
        List<File> totalFilelist = new ArrayList<>();

        Stack<File> folderStack = new Stack<>();

        folderStack.push(rootPath);

        while (!folderStack.isEmpty())
        {
            File currentFolderPath = folderStack.pop();

            List<File> folderList = listFiles(currentFolderPath);

            for (File file : folderList)
            {
                if (file.isDirectory())
                {
                    folderStack.push(file);
                }
                else
                {
                    if (filterFunction.apply(file))
                    {
                        totalFilelist.add(file);
                    }
                }
            }
        }

        return totalFilelist;
    }

    private static List<File> listFiles(File rootPath)
    {
        List<File> outputList = new ArrayList<>();

        if (rootPath.exists())
        {
            outputList = Arrays.stream(rootPath.listFiles())
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

    public static String getAbsolutePath(@NonNull File filePath)
    {
        String fullPath = filePath.getAbsolutePath();

        String[] subString = fullPath.split(ParamConfig.getFileSeparator());

        String fileNameWithExtension = subString[subString.length - 1];

        int fileStartIndex = fullPath.length() - fileNameWithExtension.length();

        return fullPath.substring(0, fileStartIndex);
    }

    public static String getFileName(@NonNull String filePath)
    {
        String[] subString = filePath.split(ParamConfig.getFileSeparator());

        String fileNameWithExtension = subString[subString.length - 1];

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

}
