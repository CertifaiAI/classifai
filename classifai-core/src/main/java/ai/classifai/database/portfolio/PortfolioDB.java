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
package ai.classifai.database.portfolio;

import ai.classifai.action.DeleteProjectData;
import ai.classifai.action.FileGenerator;
import ai.classifai.action.ProjectExport;
import ai.classifai.action.RenameProjectData;
import ai.classifai.database.DBUtils;
import ai.classifai.database.annotation.AnnotationQuery;
import ai.classifai.database.annotation.AnnotationVerticle;
import ai.classifai.database.versioning.Annotation;
import ai.classifai.database.versioning.AnnotationVersion;
import ai.classifai.database.versioning.ProjectVersion;
import ai.classifai.loader.ProjectLoader;
import ai.classifai.util.ParamConfig;
import ai.classifai.util.collection.ConversionHandler;
import ai.classifai.util.data.ImageHandler;
import ai.classifai.util.message.ReplyHandler;
import ai.classifai.util.project.ProjectHandler;
import ai.classifai.util.type.AnnotationHandler;
import ai.classifai.wasabis3.WasabiImageHandler;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * To run database query
 *
 * @author devenyantis
 */
@Slf4j
public class PortfolioDB {

    private final JDBCPool portfolioPool;
    private final FileGenerator fileGenerator = new FileGenerator();

    public PortfolioDB(JDBCPool portfolioPool) {
        this.portfolioPool = portfolioPool;
    }

    private Future<RowSet<Row>> runQuery(String query, Tuple params) {
        return runQuery(query, params, this.portfolioPool);
    }

    private Future<RowSet<Row>> runQuery(String query, Tuple params, JDBCPool pool) {
        final Promise<RowSet<Row>> promise = Promise.promise();
        pool.preparedQuery(query)
                .execute(params)
                .onComplete(fetch -> {
                    if(fetch.succeeded()) {
                        promise.complete(fetch.result());
                    } else {
                        promise.fail(fetch.cause());
                    }
                });
        return promise.future();
    }

    public Future<JsonObject> renameProject(String projectId, String newProjectName) {
        Tuple params = Tuple.of(newProjectName, projectId);

        Promise<JsonObject> promise = Promise.promise();
        runQuery(PortfolioDbQuery.getRenameProject(), params)
                .onComplete(DBUtils.handleResponse(
                        result -> promise.complete(ReplyHandler.getOkReply()),
                        promise::fail
                ));
        return promise.future();
    }

    public Future<JsonObject> exportProject(String projectId, int exportType) {
        Tuple params = Tuple.of(projectId);
        Promise<JsonObject> promise = Promise.promise();
        runQuery(PortfolioDbQuery.getExportProject(), params)
                .onComplete(DBUtils.handleResponse(
                        result -> {
                            // export project table relevant
                            ProjectLoader loader = ProjectHandler.getProjectLoader(projectId);
                            JDBCPool client = AnnotationHandler.getJDBCPool(Objects.requireNonNull(loader));

                            client.preparedQuery(AnnotationQuery.getExtractProject())
                                    .execute(params)
                                    .onComplete(annotationFetch -> {
                                        if (annotationFetch.succeeded())
                                        {
                                            JsonObject configContent = ProjectExport.getConfigContent(result,
                                                    annotationFetch.result());
                                            if(configContent == null) return;

                                            fileGenerator.run(loader, configContent, exportType);
                                        }
                                    });
                            promise.complete(ReplyHandler.getOkReply());
                        },
                        cause -> ProjectExport.setExportStatus(ProjectExport.ProjectExportStatus.EXPORT_FAIL)
                ));

        return promise.future();
    }

    public Future<JsonObject> deleteProjectData(String projectId, JsonArray uuidListArray, JsonArray uuidImgPathList) {
        ProjectLoader loader = Objects.requireNonNull(ProjectHandler.getProjectLoader(projectId));
        List<String> deleteUUIDList = ConversionHandler.jsonArray2StringList(uuidListArray);
        String uuidQueryParam = String.join(",", deleteUUIDList);

        Tuple params = Tuple.of(projectId, uuidQueryParam);
        Promise<JsonObject> promise = Promise.promise();

        runQuery(AnnotationQuery.getDeleteProjectData(), params, AnnotationHandler.getJDBCPool(loader))
                .onComplete(DBUtils.handleResponse(
                        result -> {
                            try {
                                JsonObject response = DeleteProjectData.deleteProjectDataOnComplete(
                                        loader, deleteUUIDList, uuidImgPathList);
                                promise.complete(response);
                            } catch (IOException e) {
                                String errorMessage = "Fail to delete. IO exception occurs.";
                                log.info(errorMessage);
                                promise.fail(errorMessage);
                            }
                        },
                        cause -> promise.fail("Delete project data fail")
                ));

        return promise.future();
    }

    public Future<JsonObject> renameData(String projectId, String uuid, String newFilename) {

        ProjectLoader loader = Objects.requireNonNull(ProjectHandler.getProjectLoader(projectId));
        RenameProjectData renameProjectData = new RenameProjectData(loader);
        renameProjectData.getAnnotationVersion(uuid);

        Promise<JsonObject> promise = Promise.promise();

        if(renameProjectData.containIllegalChars(newFilename)) {
            // Abort if filename contain illegal chars
            String illegalCharMes = "Contain illegal character";
            promise.fail(renameProjectData.reportRenameError(
                    RenameProjectData.RenameDataErrorCode.FILENAME_CONTAIN_ILLEGAL_CHAR.ordinal(), illegalCharMes).toString());
        }

        String updatedFileName = renameProjectData.modifyFileNameFromCache(newFilename);
        File newDataPath = renameProjectData.createNewDataPath(updatedFileName);

        if(newDataPath.exists()) {
            // Abort if name exists
            String nameExistMes = "Name exists";
            promise.fail(renameProjectData.reportRenameError(
                    RenameProjectData.RenameDataErrorCode.FILENAME_EXIST.ordinal(), nameExistMes).toString());
        }

        Tuple params = Tuple.of(updatedFileName, uuid, projectId);

        if(renameProjectData.renameDataPath(newDataPath, renameProjectData.getOldDataFileName()))
        {
            runQuery(AnnotationQuery.getRenameProjectData(), params, AnnotationHandler.getJDBCPool(loader))
                    .onComplete(DBUtils.handleResponse(
                            result -> {
                                JsonObject response = ReplyHandler.getOkReply();
                                response.put(ParamConfig.getImgPathParam(), newDataPath.toString());
                                promise.complete(response);
                            },
                            cause -> {
                                String queryErrorMes = "Fail to update filename in database";
                                promise.fail(renameProjectData.reportRenameError(
                                        RenameProjectData.RenameDataErrorCode.RENAME_FAIL.ordinal(), queryErrorMes).toString());
                            }
                    ));
            renameProjectData.updateAnnotationCache(updatedFileName, uuid);
        }
        else
        {
            String failRenameMes = "Fail to rename file";
            promise.fail(renameProjectData.reportRenameError(
                    RenameProjectData.RenameDataErrorCode.RENAME_FAIL.ordinal(), failRenameMes).toString());
        }

        return promise.future();
    }

    public Future<JsonObject> starProject(String projectId, Boolean isStarred) {
        Tuple params = Tuple.of(isStarred, projectId);
        Promise<JsonObject> promise = Promise.promise();
        runQuery(PortfolioDbQuery.getStarProject(), params)
                .onComplete(DBUtils.handleResponse(
                        result -> promise.complete(ReplyHandler.getOkReply()),
                        promise::fail
                ));
        return promise.future();
    }

    public Future<JsonObject> reloadProject(String projectId) {

        ProjectLoader loader = Objects.requireNonNull(ProjectHandler.getProjectLoader(projectId));
        Promise<JsonObject> promise = Promise.promise();

        if(ImageHandler.loadProjectRootPath(loader)) {
             promise.complete(ReplyHandler.getOkReply());
        } else {
            promise.fail(ReplyHandler.getFailedReply().toString());
        }

        return promise.future();
    }

    public Future<JsonObject> getProjectMetadata(String projectId) {

        Promise<JsonObject> promise = Promise.promise();
        List<JsonObject> result = new ArrayList<>();

        PortfolioVerticle.getProjectMetadata(result, projectId);
        JsonObject response = ReplyHandler.getOkReply()
                .put(ParamConfig.getContent(), result);
        promise.complete(response);

        return promise.future();
    }

    public Future<JsonObject> getAllProjectsMeta(int annotationType) {
        Tuple params = Tuple.of(annotationType);
        Promise<JsonObject> promise = Promise.promise();
        runQuery(PortfolioDbQuery.getRetrieveAllProjectsForAnnotationType(), params)
                .onComplete(DBUtils.handleResponse(
                        result -> {
                            List<JsonObject> projectData = new ArrayList<>();
                            for (Row row : result)
                            {
                                String projectName = row.getString(0);
                                PortfolioVerticle.getProjectMetadata(projectData,
                                        ProjectHandler.getProjectId(projectName, annotationType));
                            }
                            JsonObject response = ReplyHandler.getOkReply()
                                    .put(ParamConfig.getContent(), projectData);
                            promise.complete(response);
                        },
                        promise::fail
                ));
        return promise.future();
    }

    public Future<JsonObject> loadProject(String projectId) {
        Promise<JsonObject> promise = Promise.promise();

        ProjectLoader loader = Objects.requireNonNull(ProjectHandler.getProjectLoader(projectId));
        List<String> oriUUIDList = loader.getUuidListFromDb();
        loader.setDbOriUUIDSize(oriUUIDList.size());

        String annotationType = Objects.requireNonNull(AnnotationHandler.getType(loader.getAnnotationType())).name();
        boolean imageType = annotationType.equals("BOUNDINGBOX") || annotationType.equals("SEGMENTATION");
        String loadValidProjectUUID;

        if (imageType){
            loadValidProjectUUID = AnnotationQuery.getLoadValidProjectUuid();
        }
        else {
            loadValidProjectUUID = AnnotationQuery.getLoadValidVideoProjectUuid();
        }

        for (int i = 0; i < oriUUIDList.size(); ++i) {
            final Integer currentLength = i + 1;
            final String UUID = oriUUIDList.get(i);
            Tuple params = Tuple.of(projectId, UUID);

            runQuery(loadValidProjectUUID, params, AnnotationHandler.getJDBCPool(loader))
                    .onComplete(DBUtils.handleResponse(
                            result -> {
                                if(result.iterator().hasNext())
                                {
                                    Row row = result.iterator().next();

                                    String dataSubPath = row.getString(0);
                                    File dataFullPath = AnnotationVerticle.getDataFullPath(projectId, dataSubPath);

                                    if (ImageHandler.isImageReadable(dataFullPath))
                                    {
                                        loader.pushDBValidUUID(UUID);
                                    }
                                }
                                loader.updateDBLoadingProgress(currentLength);
                                if(!promise.future().isComplete()) {
                                    promise.complete(ReplyHandler.getOkReply());
                                }
                            },
                            promise::fail
                    ));
        }
        return promise.future();
    }

    public Future<JsonObject> getThumbnail(String projectId, String uuid) {

        Promise<JsonObject> promise = Promise.promise();
        ProjectLoader loader = Objects.requireNonNull(ProjectHandler.getProjectLoader(projectId));
        String annotationKey = PortfolioVerticle.getAnnotationKey(loader);
        String annotationType = Objects.requireNonNull(AnnotationHandler.getType(loader.getAnnotationType())).name();
        boolean imageType = annotationType.equals("BOUNDINGBOX") || annotationType.equals("SEGMENTATION");

        if (imageType){
            promise.complete(PortfolioVerticle.queryData(projectId, uuid, annotationKey));
        }
        else {
            promise.complete(PortfolioVerticle.queryVideoData(projectId, uuid, annotationKey));
        }

        return promise.future();
    }

    public Future<JsonObject> getImageSource(String projectId, String uuid, String projectName) {
        Tuple params = Tuple.of(uuid, projectId);
        ProjectLoader loader = Objects.requireNonNull(ProjectHandler.getProjectLoader(projectId));

        String annotationType = Objects.requireNonNull(AnnotationHandler.getType(loader.getAnnotationType())).name();
        boolean imageType = annotationType.equals("BOUNDINGBOX") || annotationType.equals("SEGMENTATION");
        String retrieveDataPath;

        if (imageType){
            retrieveDataPath = AnnotationQuery.getRetrieveDataPath();
        }
        else {
            retrieveDataPath = AnnotationQuery.getRetrieveVideoDataPath();
        }
        Promise<JsonObject> promise = Promise.promise();
        runQuery(retrieveDataPath, params, AnnotationHandler.getJDBCPool(loader))
                .onComplete(DBUtils.handleResponse(
                        result -> {
                            if (result.size() == 0) {
                                promise.fail("Failure to retrieve data path for " + projectName + " with uuid " + uuid);
                            } else {
                                JsonObject response = ReplyHandler.getOkReply();
                                Row row = result.iterator().next();
                                String dataPath = row.getString(0);

                                if(loader.isCloud()) {
                                    response.put(ParamConfig.getImgSrcParam(),
                                            WasabiImageHandler.encodeFileToBase64Binary(loader.getWasabiProject(), dataPath));
                                } else {
                                    File fileImgPath = AnnotationVerticle.getDataFullPath(projectId, dataPath);
                                    response.put(ParamConfig.getImgSrcParam(), ImageHandler.encodeFileToBase64Binary(fileImgPath));
                                }
                                promise.complete(response);
                            }
                        },
                        promise::fail
                ));
        return promise.future();
    }

    public Future<JsonObject> updateData(JsonObject requestBody, String projectId, Integer type) {
        Promise<JsonObject> promise = Promise.promise();
        try
        {
            String uuid = requestBody.getString(ParamConfig.getUuidParam());

            ProjectLoader loader = Objects.requireNonNull(ProjectHandler.getProjectLoader(projectId));
            Annotation annotation = loader.getUuidAnnotationDict().get(uuid);

            Integer imgDepth = requestBody.getInteger(ParamConfig.getImgDepth());
            annotation.setImgDepth(imgDepth);

            Integer imgOriW = requestBody.getInteger(ParamConfig.getImgOriWParam());
            annotation.setImgOriW(imgOriW);

            Integer imgOriH = requestBody.getInteger(ParamConfig.getImgOriHParam());
            annotation.setImgOriH(imgOriH);

            Integer fileSize = requestBody.getInteger(ParamConfig.getFileSizeParam());
            annotation.setFileSize(fileSize);

            Integer videoFrameIdx = requestBody.getInteger(ParamConfig.getVideoFrameIndexParam());
            annotation.setVideoFrameIdx(videoFrameIdx);

            Integer timeStamp = requestBody.getInteger(ParamConfig.getVideoTimeStamp());
            annotation.setTimeStamp(timeStamp);


            String currentVersionUuid = loader.getCurrentVersionUuid();

            AnnotationVersion version = annotation.getAnnotationDict().get(currentVersionUuid);

            version.setAnnotation(requestBody.getJsonArray(PortfolioVerticle.getAnnotationKey(loader)));
            version.setImgX(requestBody.getInteger(ParamConfig.getImgXParam()));
            version.setImgY(requestBody.getInteger(ParamConfig.getImgYParam()));
            version.setImgW(requestBody.getInteger(ParamConfig.getImgWParam()));
            version.setImgH(requestBody.getInteger(ParamConfig.getImgHParam()));

            String annotationType = Objects.requireNonNull(AnnotationHandler.getType(type)).name();
            boolean imageType = annotationType.equals("BOUNDINGBOX") || annotationType.equals("SEGMENTATION") ;
            String updateData;

            if(imageType) {
                updateData = AnnotationQuery.getUpdateData();
                Tuple ImageParams = Tuple.of(annotation.getAnnotationDictDbFormat(),
                        imgDepth,
                        imgOriW,
                        imgOriH,
                        fileSize,
                        uuid,
                        projectId);
                runQuery(updateData, ImageParams, AnnotationHandler.getJDBCPool(loader))
                        .onComplete(DBUtils.handleResponse(
                                result -> promise.complete(ReplyHandler.getOkReply()),
                                promise::fail
                        ));
            }
            else{
                updateData = AnnotationQuery.getUpdateVideoData();
                Tuple params = Tuple.of(annotation.getAnnotationDictDbFormat(),
                        imgDepth,
                        imgOriW,
                        imgOriH,
                        fileSize,
                        uuid,
                        projectId);
                runQuery(updateData, params, AnnotationHandler.getJDBCPool(loader))
                        .onComplete(DBUtils.handleResponse(
                                result -> promise.complete(ReplyHandler.getOkReply()),
                                promise::fail
                        ));
            }
        }
        catch (Exception e)
        {
            log.info("Update fail: " + e);
            promise.fail(e);
        }

        return promise.future();
    }

    public Future<JsonObject> updateLastModifiedDate(String projectId, String dbFormat) {

        Tuple params = Tuple.of(dbFormat, projectId);

        Promise<JsonObject> promise = Promise.promise();
        runQuery(PortfolioDbQuery.getUpdateLastModifiedDate(), params)
                .onComplete(DBUtils.handleResponse(
                   result -> promise.complete(ReplyHandler.getOkReply()),
                   cause -> {
                       log.info(cause.getMessage());
                       promise.fail(cause);
                   }
                ));
        return promise.future();
    }

    public Future<JsonObject> updateLabels(String projectId, JsonObject requestBody) {
        ProjectLoader loader = Objects.requireNonNull(ProjectHandler.getProjectLoader(projectId));
        JsonArray newLabelListJson = requestBody.getJsonArray(ParamConfig.getLabelListParam());
        ProjectVersion project = loader.getProjectVersion();

        PortfolioVerticle.updateLoaderLabelList(loader, project, newLabelListJson);

        Tuple params = Tuple.of(project.getLabelVersionDbFormat(), projectId);

        Promise<JsonObject> promise = Promise.promise();
        runQuery(PortfolioDbQuery.getUpdateLabelList(), params)
                .onComplete(DBUtils.handleResponse(
                        result -> promise.complete(ReplyHandler.getOkReply()),
                        promise::fail
                ));

        return promise.future();
    }

    public Future<JsonObject> deleteProjectFromPortfolioDb(String projectID) {
        Tuple params = Tuple.of(projectID);

        Promise<JsonObject> promise = Promise.promise();
        runQuery(PortfolioDbQuery.getDeleteProject(), params)
                .onComplete(DBUtils.handleResponse(
                        result -> promise.complete(ReplyHandler.getOkReply()),
                        promise::fail
                ));
        return promise.future();
    }

    public Future<JsonObject> deleteProjectFromAnnotationDb(String projectId) {
        ProjectLoader loader = Objects.requireNonNull(ProjectHandler.getProjectLoader(projectId));
        String annotationType = Objects.requireNonNull(AnnotationHandler.getType(loader.getAnnotationType())).name();
        boolean imageType = annotationType.equals("BOUNDINGBOX") || annotationType.equals("SEGMENTATION");
        String deleteProjectFromAnnotationDb;

        if (imageType){
            deleteProjectFromAnnotationDb = AnnotationQuery.getDeleteProject();
        }
        else {
            deleteProjectFromAnnotationDb = AnnotationQuery.getDeleteVideoProject();
        }

        Tuple params = Tuple.of(projectId);
        Promise<JsonObject> promise = Promise.promise();
        runQuery(deleteProjectFromAnnotationDb, params, AnnotationHandler.getJDBCPool(loader))
                .onComplete(DBUtils.handleResponse(
                        result -> promise.complete(ReplyHandler.getOkReply()),
                        promise::fail
                ));
        return promise.future();
    }
}
