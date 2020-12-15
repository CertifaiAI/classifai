/*
 * Copyright (c) 2020 CertifAI Sdn. Bhd.
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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * File Handler
 *
 * @author codenamewei
 */
public class FileHandler
{
    public static List<File> processFolder(@NonNull File rootPath, @NonNull String[] extensionFormat)
    {
        List<File> totalFilelist = new ArrayList<>();

        Stack<File> folderStack = new Stack<>();

        folderStack.push(rootPath);

        while(folderStack.isEmpty() != true)
        {
            File currentFolderPath = folderStack.pop();

            File[] folderList = currentFolderPath.listFiles();

            for(File file : folderList)
            {
                if (file.isDirectory())
                {
                    folderStack.push(file);
                }
                else
                {
                    if(isfileSupported(file.getAbsolutePath(), extensionFormat))
                    {
                        totalFilelist.add(file);
                    }
                }
            }
        }

        return totalFilelist;
    }

    public static boolean isfileSupported(String file, String[] formatTypes)
    {
        for(String format : formatTypes)
        {
            Integer beginIndex = file.length() - format.length();
            Integer endIndex = file.length();

            if(file.substring(beginIndex, endIndex).equals(format))
            {
                return true;
            }
        }
        return false;
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
}
