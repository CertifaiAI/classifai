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

import ai.classifai.database.DBUtils;
import ai.classifai.database.annotation.AnnotationQuery;
import ai.classifai.database.annotation.AnnotationVerticle;
import ai.classifai.database.annotation.seg.SegVerticle;
import ai.classifai.database.versioning.Annotation;
import ai.classifai.database.versioning.AnnotationVersion;
import ai.classifai.database.versioning.ProjectVersion;
import ai.classifai.loader.ProjectLoader;
import ai.classifai.util.ParamConfig;
import ai.classifai.util.data.ImageHandler;
import ai.classifai.util.message.ReplyHandler;
import ai.classifai.util.project.ProjectHandler;
import ai.classifai.util.type.AnnotationHandler;
import ai.classifai.util.type.AnnotationType;
import ai.classifai.wasabis3.WasabiImageHandler;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import lombok.extern.slf4j.Slf4j;
import io.vertx.jdbcclient.JDBCPool;

import java.awt.desktop.PreferencesEvent;
import java.io.File;
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
//    private final EventBus eventBus;
//
//    public PortfolioDB(EventBus eventBus) {
//        this.eventBus = eventBus;
//    }

    private final JDBCPool portfolioPool;

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

//    private Future<JsonObject> runQuery(JsonObject msg, String action, String annotationQueue) {
//        DeliveryOptions options = new DeliveryOptions()
//                .addHeader(ParamConfig.getActionKeyword(), action);
//
//        final Promise<JsonObject> promise = Promise.promise();
//        eventBus.request(annotationQueue, msg, options, fetch -> {
//            JsonObject response = (JsonObject) fetch.result().body();
//            promise.complete(response);
//        });
//        return promise.future();
//    }
//
//    private Future<JsonObject> runQuery(JsonObject msg, String action) {
//        return runQuery(msg, action, PortfolioDbQuery.getQueue());
//    }

//    public Future<JsonObject> renameProject(String projectId, String newProjectName) {
//        JsonObject msg = new JsonObject()
//                .put(ParamConfig.getProjectIdParam(), projectId)
//                .put(ParamConfig.getNewProjectNameParam(), newProjectName);
//
//        return runQuery(msg, PortfolioDbQuery.getRenameProject());
//    }
//
//    public Future<JsonObject> exportProject(String projectId, int annotationType, int exportType) {
//        JsonObject msg = new JsonObject()
//                .put(ParamConfig.getProjectIdParam(), projectId)
//                .put(ParamConfig.getAnnotationTypeParam(), annotationType)
//                .put(ActionConfig.getExportTypeParam(), exportType);
//
//        return runQuery(msg, PortfolioDbQuery.getExportProject());
//    }
//
//    public Future<JsonObject> deleteProjectData(String projectId, String annotationQueue,
//                                                JsonArray uuidListArray, JsonArray uuidImgPathList) {
//        JsonObject msg = new JsonObject()
//                .put(ParamConfig.getProjectIdParam(), projectId)
//                .put(ParamConfig.getUuidListParam(), uuidListArray)
//                .put(ParamConfig.getImgPathListParam(), uuidImgPathList);
//
//        return runQuery(msg, AnnotationQuery.getDeleteProjectData(), annotationQueue);
//    }
//
//    public Future<JsonObject> renameData(String projectId, String annotationQueue, String uuid, String newFilename) {
//        JsonObject msg = new JsonObject()
//                .put(ParamConfig.getProjectIdParam(), projectId)
//                .put(ParamConfig.getUuidParam(), uuid)
//                .put(ParamConfig.getNewFileNameParam(), newFilename);
//
//        return runQuery(msg, AnnotationQuery.getRenameProjectData(), annotationQueue);
//    }
//
//    public Future<JsonObject> startProject(String projectId, Boolean isStarred) {
//        JsonObject msg = new JsonObject()
//                .put(ParamConfig.getStatusParam(), isStarred)
//                .put(ParamConfig.getProjectIdParam(), projectId);
//
//        return runQuery(msg, PortfolioDbQuery.getStarProject());
//    }
//
//    public Future<JsonObject> reloadProject(String projectId) {
//        JsonObject msg = new JsonObject()
//                .put(ParamConfig.getProjectIdParam(), projectId);
//
//        return runQuery(msg, PortfolioDbQuery.getReloadProject());
//    }
//
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
//
//    public Future<JsonObject> getAllProjectsMeta(int annotationType) {
//        JsonObject msg = new JsonObject()
//                .put(ParamConfig.getAnnotationTypeParam(), annotationType);
//
//        return runQuery(msg, PortfolioDbQuery.getRetrieveAllProjectsMetadata());
//    }


    public Future<JsonObject> loadProject(String projectId) {
        Promise<JsonObject> promise = Promise.promise();

        ProjectLoader loader = Objects.requireNonNull(ProjectHandler.getProjectLoader(projectId));
        List<String> oriUUIDList = loader.getUuidListFromDb();
        loader.setDbOriUUIDSize(oriUUIDList.size());

        for (int i = 0; i < oriUUIDList.size(); ++i) {
            final Integer currentLength = i + 1;
            final String UUID = oriUUIDList.get(i);
            Tuple params = Tuple.of(projectId, UUID);

            runQuery(AnnotationQuery.getLoadValidProjectUuid(), params, AnnotationHandler.getJDBCPool(loader))
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

//    public Future<JsonObject> loadProject(String projectId, String annotationQueue) {
//        JsonObject msg = new JsonObject().put(ParamConfig.getProjectIdParam(), projectId);
//
//        return runQuery(msg, AnnotationQuery.getLoadValidProjectUuid(), annotationQueue);
//    }


    public Future<JsonObject> getThumbnail(String projectId, String uuid) {
        Promise<JsonObject> promise = Promise.promise();
        ProjectLoader loader = Objects.requireNonNull(ProjectHandler.getProjectLoader(projectId));

        String annotationKey = PortfolioVerticle.getAnnotationKey(loader);

        promise.complete(PortfolioVerticle.queryData(projectId, uuid, annotationKey));

        return promise.future();
    }

//    public Future<JsonObject> getThumbnail(String projectID, String annotationQueue, String uuid) {
//        JsonObject msg = new JsonObject()
//                .put(ParamConfig.getUuidParam(), uuid)
//                .put(ParamConfig.getProjectIdParam(), projectID);
//
//        return runQuery(msg, AnnotationQuery.getQueryData(), annotationQueue);
//    }

    public Future<JsonObject> getImageSource(String projectId, String uuid, String projectName) {
        Tuple params = Tuple.of(uuid, projectId);
        ProjectLoader loader = Objects.requireNonNull(ProjectHandler.getProjectLoader(projectId));
        Promise<JsonObject> promise = Promise.promise();
        runQuery(AnnotationQuery.getRetrieveDataPath(), params, AnnotationHandler.getJDBCPool(loader))
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

//
//    public Future<JsonObject> getImageSource(String projectID, String annotationQueue,
//                                             String uuid, String projectName) {
//        JsonObject msg = new JsonObject()
//                .put(ParamConfig.getUuidParam(), uuid)
//                .put(ParamConfig.getProjectIdParam(), projectID)
//                .put(ParamConfig.getProjectNameParam(), projectName);
//
//        return runQuery(msg, AnnotationQuery.getRetrieveDataPath(), annotationQueue);
//    }

    public Future<JsonObject> updateData(JsonObject requestBody, String projectId) {
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

            String currentVersionUuid = loader.getCurrentVersionUuid();

            AnnotationVersion version = annotation.getAnnotationDict().get(currentVersionUuid);

            version.setAnnotation(requestBody.getJsonArray(PortfolioVerticle.getAnnotationKey(loader)));
            version.setImgX(requestBody.getInteger(ParamConfig.getImgXParam()));
            version.setImgY(requestBody.getInteger(ParamConfig.getImgYParam()));
            version.setImgW(requestBody.getInteger(ParamConfig.getImgWParam()));
            version.setImgH(requestBody.getInteger(ParamConfig.getImgHParam()));

            Tuple params = Tuple.of(annotation.getAnnotationDictDbFormat(),
                    imgDepth,
                    imgOriW,
                    imgOriH,
                    fileSize,
                    uuid,
                    projectId);

            runQuery(AnnotationQuery.getUpdateData(), params, AnnotationHandler.getJDBCPool(loader))
                    .onComplete(DBUtils.handleResponse(
                            result -> promise.complete(ReplyHandler.getOkReply()),
                            promise::fail
                    ));
        }
        catch (Exception e)
        {
            log.info("Update fail: " + e);
            promise.fail(e);
        }

        return promise.future();
    }

//
//    public Future<JsonObject> updateData(String projectID, String annotationQueue, JsonObject requestBody) {
//        requestBody.put(ParamConfig.getProjectIdParam(), projectID);
//
//        return runQuery(requestBody, AnnotationQuery.getUpdateData(), annotationQueue);
//    }

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
//
//    public Future<JsonObject> updateLastModifiedDate(String projectID, String dbFormat) {
//        JsonObject msg = new JsonObject()
//                .put(ParamConfig.getProjectIdParam(), projectID)
//                .put(ParamConfig.getCurrentVersionParam(), dbFormat);
//
//        return runQuery(msg, PortfolioDbQuery.getUpdateLastModifiedDate());
//    }
//


    public Future<JsonObject> updateLabels(String projectId, JsonObject requestBody) {
        ProjectLoader loader = Objects.requireNonNull(ProjectHandler.getProjectLoader(projectId));
        JsonArray newLabelListJson = new JsonArray(requestBody.getString(ParamConfig.getLabelListParam()));
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



//    public Future<JsonObject> updateLabels(String projectID, JsonObject requestBody) {
//        requestBody.put(ParamConfig.getProjectIdParam(), projectID);
//
//        return runQuery(requestBody, PortfolioDbQuery.getUpdateLabelList());
//    }
//
//    public Future<JsonObject> deleteProjectFromPortfolioDb(String projectID) {
//        JsonObject msg = new JsonObject()
//                .put(ParamConfig.getProjectIdParam(), projectID);
//
//        return runQuery(msg, PortfolioDbQuery.getDeleteProject());
//    }
//
//    public Future<JsonObject> deleteProjectFromAnnotationDb(String projectId, String annotationQuery) {
//        JsonObject msg = new JsonObject()
//                .put(ParamConfig.getProjectIdParam(), projectId);
//
//        return runQuery(msg, AnnotationQuery.getDeleteProject(),annotationQuery);
//    }
}
