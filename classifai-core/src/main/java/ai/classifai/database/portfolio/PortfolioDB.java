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
import ai.classifai.dto.ProjectConfigProperties;
import ai.classifai.dto.ProjectMetaProperties;
import ai.classifai.dto.ThumbnailProperties;
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

    public Future<Void> renameProject(String projectId, String newProjectName) {
        Tuple params = Tuple.of(newProjectName, projectId);

        return runQuery(PortfolioDbQuery.getRenameProject(), params)
                .map(DBUtils::toVoid);
    }

    public Future<Void> exportProject(String projectId, int exportType) {
        Tuple params = Tuple.of(projectId);
        Promise<Void> promise = Promise.promise();
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
                                            ProjectConfigProperties configContent = ProjectExport.getConfigContent(result,
                                                    annotationFetch.result());
                                            if(configContent == null) return;

                                            fileGenerator.run(loader, configContent, exportType);
                                        }
                                    });
                            promise.complete();
                        },
                        cause -> ProjectExport.setExportStatus(ProjectExport.ProjectExportStatus.EXPORT_FAIL)
                ));

        return promise.future();
    }

    public Future<List<String>> deleteProjectData(String projectId, List<String> deleteUUIDList, List<String> uuidImgPathList) {
        ProjectLoader loader = Objects.requireNonNull(ProjectHandler.getProjectLoader(projectId));
        String uuidQueryParam = String.join(",", deleteUUIDList);

        Tuple params = Tuple.of(projectId, uuidQueryParam);

        return runQuery(AnnotationQuery.getDeleteProjectData(), params, AnnotationHandler.getJDBCPool(loader))
                .map(result -> {
                    try {
                        return DeleteProjectData.deleteProjectDataOnComplete(loader, deleteUUIDList, uuidImgPathList);
                    } catch (IOException e) {
                        log.info("Fail to delete. IO exception occurs.");
                    }
                    return loader.getSanityUuidList();
                });
    }

    public Future<String> renameData(String projectId, String uuid, String newFilename) {

        ProjectLoader loader = Objects.requireNonNull(ProjectHandler.getProjectLoader(projectId));
        RenameProjectData renameProjectData = new RenameProjectData(loader);
        renameProjectData.getAnnotationVersion(uuid);

        Promise<String> promise = Promise.promise();

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

            return runQuery(AnnotationQuery.getRenameProjectData(), params, AnnotationHandler.getJDBCPool(loader))
                    .map(result -> {
                        renameProjectData.updateAnnotationCache(updatedFileName, uuid);
                        return newDataPath.toString();
                    });
        }

        String failRenameMes = "Fail to rename file";
        if(!promise.future().isComplete()) {
            promise.fail(renameProjectData.reportRenameError(
                    RenameProjectData.RenameDataErrorCode.RENAME_FAIL.ordinal(), failRenameMes).toString());
        }

        return promise.future();
    }

    public Future<Void> starProject(String projectId, Boolean isStarred) {
        Tuple params = Tuple.of(isStarred, projectId);

        return runQuery(PortfolioDbQuery.getStarProject(), params)
                .map(DBUtils::toVoid);
    }

    public Future<Void> reloadProject(String projectId) {

        ProjectLoader loader = Objects.requireNonNull(ProjectHandler.getProjectLoader(projectId));
        Promise<Void> promise = Promise.promise();

        if(ImageHandler.loadProjectRootPath(loader)) {
            promise.complete();
        } else {
            promise.fail(ReplyHandler.getFailedReply().toString());
        }

        return promise.future();
    }

    public Future<List<ProjectMetaProperties>> getProjectMetadata(String projectId) {

        Promise<List<ProjectMetaProperties>> promise = Promise.promise();
        List<ProjectMetaProperties> result = new ArrayList<>();

        PortfolioVerticle.getProjectMetadata(result, projectId);
        promise.complete(result);

        return promise.future();
    }

    public Future<List<ProjectMetaProperties>> getAllProjectsMeta(int annotationType) {
        Tuple params = Tuple.of(annotationType);

        return runQuery(PortfolioDbQuery.getRetrieveAllProjectsForAnnotationType(), params)
                .map(result -> {
                    List<ProjectMetaProperties> projectData = new ArrayList<>();
                    for (Row row : result)
                    {
                        String projectName = row.getString(0);
                        PortfolioVerticle.getProjectMetadata(projectData,
                                ProjectHandler.getProjectId(projectName, annotationType));
                    }
                    return projectData;
                });
    }

    public Future<Void> loadProject(String projectId) {
        Promise<Void> promise = Promise.promise();

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
                                    promise.complete();
                                }
                            },
                            promise::fail
                    ));
        }
        return promise.future();
    }

    public Future<ThumbnailProperties> getThumbnail(String projectId, String uuid) {
        Promise<ThumbnailProperties> promise = Promise.promise();
        ProjectLoader loader = Objects.requireNonNull(ProjectHandler.getProjectLoader(projectId));

        String annotationKey = PortfolioVerticle.getAnnotationKey(loader);

        promise.complete(PortfolioVerticle.queryData(projectId, uuid, annotationKey));

        return promise.future();
    }

    public Future<String> getImageSource(String projectId, String uuid, String projectName) {
        Tuple params = Tuple.of(uuid, projectId);
        ProjectLoader loader = Objects.requireNonNull(ProjectHandler.getProjectLoader(projectId));

        return runQuery(AnnotationQuery.getRetrieveDataPath(), params, AnnotationHandler.getJDBCPool(loader))
                .map(result -> {
                    if(result.size() != 0) {
                        String imageStr;
                        Row row = result.iterator().next();
                        String dataPath = row.getString(0);

                        if(loader.isCloud()) {
                            imageStr = WasabiImageHandler.encodeFileToBase64Binary(loader.getWasabiProject(), dataPath);
                        } else {
                            File fileImgPath = AnnotationVerticle.getDataFullPath(projectId, dataPath);
                            imageStr = ImageHandler.encodeFileToBase64Binary(fileImgPath);
                        }
                        return imageStr;
                    }

                    log.info("Failure to retrieve data path for " + projectName + " with uuid " + uuid);
                    return null;
                });
    }

    public Future<Void> updateData(JsonObject requestBody, String projectId) {
        Promise<Void> promise = Promise.promise();
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

            return runQuery(AnnotationQuery.getUpdateData(), params, AnnotationHandler.getJDBCPool(loader))
                    .map(DBUtils::toVoid);
        }
        catch (Exception e)
        {
            log.info("Update fail: " + e);
            promise.fail(e);
        }

        return promise.future();
    }

    public Future<Void> updateLastModifiedDate(String projectId, String dbFormat) {

        Tuple params = Tuple.of(dbFormat, projectId);

        return runQuery(PortfolioDbQuery.getUpdateLastModifiedDate(), params)
                .map(DBUtils::toVoid);
    }

    public Future<Void> updateLabels(String projectId, JsonObject requestBody) {
        ProjectLoader loader = Objects.requireNonNull(ProjectHandler.getProjectLoader(projectId));
        List<String> newLabelListJson = ConversionHandler.jsonArray2StringList(requestBody.getJsonArray(ParamConfig.getLabelListParam()));
        ProjectVersion project = loader.getProjectVersion();

        PortfolioVerticle.updateLoaderLabelList(loader, project, newLabelListJson);

        Tuple params = Tuple.of(project.getLabelVersionDbFormat(), projectId);

        return runQuery(PortfolioDbQuery.getUpdateLabelList(), params)
                .map(DBUtils::toVoid);
    }

    public Future<Void> deleteProjectFromPortfolioDb(String projectID) {
        Tuple params = Tuple.of(projectID);

        return runQuery(PortfolioDbQuery.getDeleteProject(), params)
                .map(DBUtils::toVoid);
    }

    public Future<Void> deleteProjectFromAnnotationDb(String projectId) {
        ProjectLoader loader = Objects.requireNonNull(ProjectHandler.getProjectLoader(projectId));
        Tuple params = Tuple.of(projectId);

        return runQuery(AnnotationQuery.getDeleteProject(), params, AnnotationHandler.getJDBCPool(loader))
                .map(DBUtils::toVoid);
    }
}
