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

import ai.classifai.database.annotation.AnnotationQuery;
import ai.classifai.database.versioning.Annotation;
import ai.classifai.loader.ProjectLoader;
import ai.classifai.util.ParamConfig;
import ai.classifai.util.message.ErrorCodes;
import ai.classifai.util.message.ReplyHandler;
import ai.classifai.util.project.ProjectHandler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.Tuple;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for renaming data points
 *
 * @author devenyantis
 */
@Slf4j
public final class RenameProjectData {

    private enum RenameDataErrorCode {
        RENAME_FAIL,
        FILENAME_EXIST,
        FILENAME_CONTAIN_ILLEGAL_CHAR,
        RENAME_SUCCESS
    }

    private static ProjectLoader loader;
    private static String dataUUID;
    private static Annotation annotation;

    private RenameProjectData() {
        throw new IllegalStateException("Utility class");
    }

    public static void renameProjectData(JDBCPool jdbcPool, Message<JsonObject> message)
    {
        String projectId = message.body().getString(ParamConfig.getProjectIdParam());
        loader = Objects.requireNonNull(ProjectHandler.getProjectLoader(projectId));
        dataUUID = message.body().getString(ParamConfig.getUuidParam());
        getAnnotationVersion();

        String newDataFileName = message.body().getString(ParamConfig.getNewFileNameParam());
        if(containIllegalChars(newDataFileName)) {
            // Abort if filename contain illegal chars
            String illegal_char_str = "Contain illegal character";
            message.reply(reportRenameError(RenameDataErrorCode.FILENAME_CONTAIN_ILLEGAL_CHAR.ordinal(), illegal_char_str));
            return;
        }

        String oldDataFileName = getOldDataFileName();

        String updatedFileName = modifyFileNameFromCache(newDataFileName);
        File newDataPath = createNewDataPath(updatedFileName);

        if(newDataPath.exists()) {
            // Abort if name exists
            String name_exist_str = "Name exists";
            message.reply(reportRenameError(RenameDataErrorCode.FILENAME_EXIST.ordinal(), name_exist_str));
            return;
        }

        Tuple params = Tuple.of(updatedFileName, dataUUID, projectId);

        if(renameDataPath(newDataPath, oldDataFileName))
        {
            invokeJDBCPool(jdbcPool, message, params, newDataPath.toString());
            updateAnnotationCache(updatedFileName);
        }
        else
        {
            String fail_rename_str = "Fail to rename file";
            message.reply(reportRenameError(RenameDataErrorCode.RENAME_FAIL.ordinal(), fail_rename_str));
        }

    }

    private static boolean containIllegalChars(String filename) {
        Pattern pattern = Pattern.compile("[ `&:;'?$~#@=*+%{}<>/\\[\\]|\"^]");
        Matcher matcher = pattern.matcher(filename);

        return matcher.find();
    }

    private static void invokeJDBCPool(JDBCPool jdbcPool, Message<JsonObject> message, Tuple params, String newDataPath)
    {
        jdbcPool.preparedQuery(AnnotationQuery.getRenameProjectData())
                .execute(params)
                .onComplete(fetch -> {
                    if(fetch.succeeded())
                    {
                        JsonObject response = ReplyHandler.getOkReply();
                        response.put(ParamConfig.getImgPathParam(), newDataPath);
                        message.replyAndRequest(response);
                    }
                    else
                    {
                        String query_error_str = "Fail to update filename in database";
                        message.reply(reportRenameError(RenameDataErrorCode.RENAME_FAIL.ordinal(), query_error_str));
                    }
                });
    }

    private static boolean renameDataPath(File newDataPath, String oldDataFileName)
    {
        File oldDataPath = new File(oldDataFileName);

        log.debug("Rename file:\nFrom: " + oldDataPath + "\nTo: " + newDataPath);

        return oldDataPath.renameTo(newDataPath);
    }

    private static String getOldDataFileName()
    {
        return Paths.get(loader.getProjectPath().toString(), annotation.getImgPath()).toString();
    }

    private static String modifyFileNameFromCache(String newFileName)
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

    private static File createNewDataPath(String newDataFileName)
    {
        File newDataPath = Paths.get(
                loader.getProjectPath().toString(), newDataFileName).toFile();
        log.debug("New data path: " + newDataPath);

        return newDataPath;
    }

    private static void getAnnotationVersion()
    {
        Map<String, Annotation> uuidAnnotationDict = loader.getUuidAnnotationDict();
        annotation = uuidAnnotationDict.get(dataUUID);
    }

    private static void updateAnnotationCache(String newImagePath)
    {
        Map<String, Annotation> uuidAnnotationDict = loader.getUuidAnnotationDict();

        annotation.setImgPath(newImagePath);
        uuidAnnotationDict.put(dataUUID, annotation);

        loader.setUuidAnnotationDict(uuidAnnotationDict);

    }

    private static JsonObject reportRenameError(int errorKey, String errorMessage)
    {
        log.info(errorMessage);

        return new JsonObject().put("message", 0)
                .put("error_code", errorKey)
                .put("error_message", errorMessage);
    }
}
