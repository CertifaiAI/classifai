/*
 * Copyright (c) 2020 CertifAI
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

package ai.certifai.util;

import ai.certifai.annotation.AnnotationOutput;
import ai.certifai.annotation.AnnotationType;
import ai.certifai.data.DataCollection;
import ai.certifai.data.DataType;
import ai.certifai.data.type.image.ImageDataCollection;
import ai.certifai.data.type.image.ImageFileType;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

@Slf4j
public class FileUtils
{
    @Setter //temporary
    private static Integer fileCount = 0;

    public static Integer generateUniqueId()
    {
        ;
        return (fileCount += 1);
    }

    public static List<File> getFiles(@NonNull String rootDataPath, DataType dataType)
    {
        List<File> filesList = new ArrayList<>();
        Stack<File> folderStack = new Stack<>();

        folderStack.push(new File(rootDataPath));

        List<String> acceptableFileFormats = Arrays.asList(ImageFileType.getImageFileTypes());

        while(folderStack.isEmpty() != true)
        {
            File currentFolderPath = folderStack.pop();

            File[] folderList = currentFolderPath.listFiles();

            try
            {
                for(File file : folderList)
                {
                    if (file.isDirectory())
                    {
                        folderStack.push(file);
                    }
                    else
                    {
                        String absPath = file.getAbsolutePath();

                        for (String allowedFormat : acceptableFileFormats)
                        {
                            if(absPath.length() > allowedFormat.length())
                            {
                                String currentFormat = absPath.substring(absPath.length()  - allowedFormat.length());

                                if(currentFormat.equals(allowedFormat))
                                {
                                    filesList.add(file);
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            catch(Exception e)
            {
                log.info("Error occured while iterating data paths: ", e);
            }

        }
        return filesList;
    }

    public static DataCollection initDataSet(@NonNull String dataRootPath, DataType dataType, AnnotationType annotationType)
    {
        DataCollection dataCollection;

        if(dataType == DataType.IMAGE)
        {
            dataCollection = new ImageDataCollection(dataRootPath, dataType, annotationType);

            return dataCollection;
        }
        return null;
    }

    //FileUtils.writeStringToFile(content, "//Users//wei//Downloads//labeledtxt", AnnotationOutput.YOLO);
    public static boolean writeStringToFile(@NonNull String content, @NonNull String filePath, AnnotationOutput output)
    {
        String fileFormat;

        if(output == AnnotationOutput.PASCALVOC)
        {
            //TODO: CHECK IF THIS IS RIGHT
            fileFormat = ".xml";

        }
        else if(output == AnnotationOutput.YOLO)
        {
            fileFormat = ".txt";
        }
        else
        {
            throw new AssertionError("Unrecognised annotation output for file saving");
        }

        try
        {
            File saveToFile = new File(filePath + fileFormat);
            org.apache.commons.io.FileUtils.writeStringToFile(saveToFile, content, StandardCharsets.UTF_8);

            log.info("Saved To File: " + saveToFile.getAbsolutePath());
            return true;
        }
        catch(Exception e)
        {
            log.error("Failed in writing file, ", e);
            e.printStackTrace();

            return false;
        }

    }
}
