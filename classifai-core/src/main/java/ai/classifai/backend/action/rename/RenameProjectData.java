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
package ai.classifai.backend.action.rename;

import ai.classifai.backend.database.versioning.Annotation;
import ai.classifai.core.entities.response.RenameDataResponse;
import ai.classifai.core.loader.ProjectLoader;
import ai.classifai.core.util.message.ReplyHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Paths;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for renaming data points
 *
 * @author devenyantis
 */
@Slf4j
public final class RenameProjectData {

    private final ProjectLoader loader;
    private Annotation annotation;

    public RenameProjectData(ProjectLoader loader) {
        this.loader = loader;
    }

    public boolean containIllegalChars(String filename) {
        Pattern pattern = Pattern.compile("[ `&:;'?$~#@=*+%{}<>/\\[\\]|\"^]");
        Matcher matcher = pattern.matcher(filename);

        return matcher.find();
    }

    public boolean renameDataPath(File newDataPath, String oldDataFileName)
    {
        File oldDataPath = new File(oldDataFileName);

        log.debug("Rename file:\nFrom: " + oldDataPath + "\nTo: " + newDataPath);

        return oldDataPath.renameTo(newDataPath);
    }

    public String getOldDataFileName()
    {
        return Paths.get(loader.getProjectPath().toString(), annotation.getImgPath()).toString();
    }

    public String modifyFileNameFromCache(String newFileName)
    {
        String oldDataPath = annotation.getImgPath();
        // get only the filename after last slash before file extension
        String oldDataPathFileName = oldDataPath.substring(oldDataPath.lastIndexOf(File.separator) + 1, oldDataPath.lastIndexOf("."));

        String newDataPathFName = newFileName.substring(newFileName.lastIndexOf(File.separator) + 1, newFileName.lastIndexOf("."));

        // Rename old data path filename
        String newDataPathModified = oldDataPath.replace(oldDataPathFileName, newDataPathFName);
        log.debug("New modified path: " + newDataPathModified);

        return newDataPathModified;
    }

    public File createNewDataPath(String newDataFileName)
    {
        File newDataPath = Paths.get(
                loader.getProjectPath().toString(), newDataFileName).toFile();
        log.debug("New data path: " + newDataPath);

        return newDataPath;
    }

    public void getAnnotationVersion(String dataUUID)
    {
        Map<String, Annotation> uuidAnnotationDict = loader.getUuidAnnotationDict();
        annotation = uuidAnnotationDict.get(dataUUID);
    }

    public void updateAnnotationCache(String newImagePath, String dataUUID)
    {
        Map<String, Annotation> uuidAnnotationDict = loader.getUuidAnnotationDict();

        annotation.setImgPath(newImagePath);
        uuidAnnotationDict.put(dataUUID, annotation);

        loader.setUuidAnnotationDict(uuidAnnotationDict);

    }

    public static RenameDataResponse reportRenameError(String errorKeyStr)
    {
        RenameDataErrorCode errorKey = RenameDataErrorCode.valueOf(errorKeyStr);

        return RenameDataResponse.builder()
                .message(ReplyHandler.FAILED)
                .errorCode(errorKey.ordinal())
                .errorMessage(errorKey.getErrorMessage())
                .build();
    }
}
