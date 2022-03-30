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

import ai.classifai.action.ActionOps;
import ai.classifai.action.FileGenerator;
import ai.classifai.action.FileMover;
import ai.classifai.action.ProjectExport;
import ai.classifai.action.parser.PortfolioParser;
import ai.classifai.action.rename.RenameDataErrorCode;
import ai.classifai.action.rename.RenameProjectData;
import ai.classifai.database.DBUtils;
import ai.classifai.database.JDBCPoolHolder;
import ai.classifai.database.annotation.AnnotationDB;
import ai.classifai.database.annotation.AnnotationQuery;
import ai.classifai.database.annotation.TabularAnnotationQuery;
import ai.classifai.database.versioning.Annotation;
import ai.classifai.database.versioning.ProjectVersion;
import ai.classifai.database.versioning.Version;
import ai.classifai.dto.api.body.UpdateTabularDataBody;
import ai.classifai.dto.api.response.ProjectStatisticResponse;
import ai.classifai.dto.data.*;
import ai.classifai.loader.ProjectLoader;
import ai.classifai.loader.ProjectLoaderStatus;
import ai.classifai.util.ParamConfig;
import ai.classifai.util.data.ImageHandler;
import ai.classifai.util.data.LabelListHandler;
import ai.classifai.util.data.StringHandler;
import ai.classifai.util.data.TabularHandler;
import ai.classifai.util.message.ReplyHandler;
import ai.classifai.util.project.ProjectHandler;
import ai.classifai.util.project.ProjectInfra;
import ai.classifai.util.type.AnnotationType;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * To run database query
 *
 * @author devenyantis
 */
@Slf4j
public class PortfolioDB {

    private final FileGenerator fileGenerator = new FileGenerator();
    private final ProjectHandler projectHandler;
    private final ProjectExport projectExport;
    private final AnnotationDB annotationDB;
    private final JDBCPoolHolder holder;

    public PortfolioDB(JDBCPoolHolder holder, ProjectHandler projectHandler, ProjectExport projectExport, AnnotationDB annotationDB) {
        this.holder = holder;

        this.projectHandler = projectHandler;
        this.projectExport = projectExport;
        this.annotationDB = annotationDB;
    }

    private Future<RowSet<Row>> runQuery(String query, Tuple params) {
        return runQuery(query, params, this.holder.getPortfolioPool());
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

    private Future<RowSet<Row>> runQuery(String query, JDBCPool pool) {
        final Promise<RowSet<Row>> promise = Promise.promise();
        pool.preparedQuery(query)
                .execute()
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
        ProjectLoader loader = projectHandler.getProjectLoader(projectId);
        AnnotationType type = AnnotationType.get(loader.getAnnotationType());

        if(type == AnnotationType.TABULAR) {
            TabularAnnotationQuery.createChangeProjectTableNamePreparedStatement(loader, newProjectName);
            String query = TabularAnnotationQuery.getChangeProjectTableNameQuery();
            runQuery(query, this.holder.getJDBCPool(loader))
                    .map(DBUtils::toVoid);
        }

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
                            ProjectLoader loader = projectHandler.getProjectLoader(projectId);
                            JDBCPool client = holder.getJDBCPool(Objects.requireNonNull(loader));

                            client.preparedQuery(AnnotationQuery.getExtractProject())
                                    .execute(params)
                                    .onComplete(annotationFetch -> {
                                        if (annotationFetch.succeeded())
                                        {
                                            ProjectConfigProperties configContent = projectExport.getConfigContent(result,
                                                    annotationFetch.result());
                                            if(configContent == null) return;

                                            fileGenerator.run(projectExport, loader, configContent, exportType);
                                        }
                                    });
                            promise.complete();
                        },
                        cause -> {
                            projectExport.setExportStatus(ProjectExport.ProjectExportStatus.EXPORT_FAIL);
                            log.info("Project export fail", cause);
                        }
                ));

        return promise.future();
    }

    public Future<List<String>> deleteProjectData(String projectId, List<String> deleteUUIDList, List<String> uuidImgPathList) {
        ProjectLoader loader = Objects.requireNonNull(projectHandler.getProjectLoader(projectId));
        String uuidQueryParam = String.join(",", deleteUUIDList);

        Tuple params = Tuple.of(projectId, uuidQueryParam);

        return runQuery(AnnotationQuery.getDeleteProjectData(), params, holder.getJDBCPool(loader))
                .map(result -> {
                    try {
                        return deleteProjectDataOnComplete(loader, deleteUUIDList, uuidImgPathList);
                    } catch (IOException e) {
                        log.info("Fail to delete. IO exception occurs.");
                    }
                    return loader.getSanityUuidList();
                });
    }

    public Future<String> renameData(String projectId, String uuid, String newFilename) {

        ProjectLoader loader = Objects.requireNonNull(projectHandler.getProjectLoader(projectId));
        RenameProjectData renameProjectData = new RenameProjectData(loader);
        renameProjectData.getAnnotationVersion(uuid);

        Promise<String> promise = Promise.promise();

        if(renameProjectData.containIllegalChars(newFilename)) {
            // Abort if filename contain illegal chars
            promise.fail(RenameDataErrorCode.FILENAME_CONTAIN_ILLEGAL_CHAR.toString());
        }

        String updatedFileName = renameProjectData.modifyFileNameFromCache(newFilename);
        File newDataPath = renameProjectData.createNewDataPath(updatedFileName);

        if(newDataPath.exists()) {
            // Abort if name exists
            promise.fail(RenameDataErrorCode.FILENAME_EXIST.toString());
        }

        Tuple params = Tuple.of(updatedFileName, uuid, projectId);

        if(renameProjectData.renameDataPath(newDataPath, renameProjectData.getOldDataFileName()))
        {
            return runQuery(AnnotationQuery.getRenameProjectData(), params, holder.getJDBCPool(loader))
                    .map(result -> {
                        renameProjectData.updateAnnotationCache(updatedFileName, uuid);
                        return newDataPath.toString();
                    });
        }

        if(!promise.future().isComplete()) {
            promise.fail(RenameDataErrorCode.RENAME_FAIL.toString());
        }

        return promise.future();
    }

    public Future<Void> starProject(String projectId, Boolean isStarred) {
        Tuple params = Tuple.of(isStarred, projectId);

        return runQuery(PortfolioDbQuery.getStarProject(), params)
                .map(DBUtils::toVoid);
    }

    public Future<Void> reloadProject(String projectId) {

        ProjectLoader loader = Objects.requireNonNull(projectHandler.getProjectLoader(projectId));
        Promise<Void> promise = Promise.promise();

        if(ImageHandler.loadProjectRootPath(loader, annotationDB)) {
            promise.complete();
        } else {
            promise.fail(ReplyHandler.getFailedReply().toString());
        }

        return promise.future();
    }

    public Future<List<ProjectMetaProperties>> getProjectMetadata(String projectId) {

        Promise<List<ProjectMetaProperties>> promise = Promise.promise();
        List<ProjectMetaProperties> result = new ArrayList<>();

        getProjectMetadata(result, projectId);
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
                        getProjectMetadata(projectData,
                                projectHandler.getProjectId(projectName, annotationType));
                    }
                    return projectData;
                });
    }

    public Future<Void> loadProject(String projectId) {
        Promise<Void> promise = Promise.promise();

        ProjectLoader loader = Objects.requireNonNull(projectHandler.getProjectLoader(projectId));
        List<String> oriUUIDList = loader.getUuidListFromDb();
        loader.setDbOriUUIDSize(oriUUIDList.size());
        AnnotationType annotationType = AnnotationType.get(loader.getAnnotationType());

        for (int i = 0; i < oriUUIDList.size(); ++i) {
            final Integer currentLength = i + 1;
            final String UUID = oriUUIDList.get(i);
            Tuple params = Tuple.of(projectId, UUID);

            if (Objects.requireNonNull(annotationType).equals(AnnotationType.TABULAR)) {
                loader.pushDBValidUUID(UUID);
                loader.updateDBLoadingProgress(currentLength);
                if(!promise.future().isComplete()) {
                    promise.complete();
                }
            } else {
                runQuery(AnnotationQuery.getLoadValidProjectUuid(), params, holder.getJDBCPool(loader))
                        .onComplete(DBUtils.handleResponse(
                                result -> {
                                    if(result.iterator().hasNext())
                                    {
                                        Row row = result.iterator().next();

                                        String dataSubPath = row.getString(0);
                                        File dataFullPath = loader.getDataFullPath(dataSubPath);

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
        }
        return promise.future();
    }

    public Future<ThumbnailProperties> getThumbnail(String projectId, String uuid) {
        Promise<ThumbnailProperties> promise = Promise.promise();
        ProjectLoader loader = Objects.requireNonNull(projectHandler.getProjectLoader(projectId));

        String annotationKey = loader.getAnnotationKey();

        promise.complete(queryData(projectId, uuid, annotationKey));

        return promise.future();
    }

    public Future<String> getImageSource(String projectId, String uuid, String projectName) {
        Tuple params = Tuple.of(uuid, projectId);
        ProjectLoader loader = Objects.requireNonNull(projectHandler.getProjectLoader(projectId));

        return runQuery(AnnotationQuery.getRetrieveDataPath(), params, holder.getJDBCPool(loader))
                .map(result -> {
                    if(result.size() != 0) {

                        Row row = result.iterator().next();
                        String dataPath = row.getString(0);
                        File fileImgPath = loader.getDataFullPath(dataPath);

                        return ImageHandler.encodeFileToBase64Binary(fileImgPath);
                    }

                    log.info("Failure to retrieve data path for " + projectName + " with uuid " + uuid);
                    return null;
                });
    }

    public Future<Void> updateData(ThumbnailProperties requestBody, String projectId) {
        Promise<Void> promise = Promise.promise();
        try
        {
            String uuid = requestBody.getUuidParam();

            ProjectLoader loader = Objects.requireNonNull(projectHandler.getProjectLoader(projectId));
            Annotation annotation = loader.getUuidAnnotationDict().get(uuid);

            annotation.setImgDepth(requestBody.getImgDepth());
            annotation.setImgOriW(requestBody.getImgOriWParam());
            annotation.setImgOriH(requestBody.getImgOriHParam());
            annotation.setFileSize(requestBody.getFileSizeParam());

            String currentVersionUuid = loader.getCurrentVersionUuid();

            DataInfoProperties version = annotation.getAnnotationDict().get(currentVersionUuid);

            if(loader.getAnnotationKey().equals(ParamConfig.getBoundingBoxParam())) {
                version.setAnnotation(requestBody.getBoundingBoxParam());
            } else if(loader.getAnnotationKey().equals(ParamConfig.getSegmentationParam())) {
                version.setAnnotation(requestBody.getSegmentationParam());
            }

            version.setImgX(requestBody.getImgXParam());
            version.setImgY(requestBody.getImgYParam());
            version.setImgW(requestBody.getImgWParam());
            version.setImgH(requestBody.getImgHParam());

            Tuple params = Tuple.of(annotation.getAnnotationDictDbFormat(),
                    requestBody.getImgDepth(),
                    requestBody.getImgOriWParam(),
                    requestBody.getImgOriHParam(),
                    requestBody.getFileSizeParam(),
                    uuid,
                    projectId);

            return runQuery(AnnotationQuery.getUpdateData(), params, holder.getJDBCPool(loader))
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

    public Future<Void> updateLabels(String projectId, @NonNull List<String> labelList) {
        ProjectLoader loader = Objects.requireNonNull(projectHandler.getProjectLoader(projectId));
        ProjectVersion project = loader.getProjectVersion();

        updateLoaderLabelList(loader, project, labelList);

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
        ProjectLoader loader = Objects.requireNonNull(projectHandler.getProjectLoader(projectId));
        AnnotationType annotationType = AnnotationType.get(loader.getAnnotationType());
        Tuple params = Tuple.of(projectId);

        if (Objects.requireNonNull(annotationType).equals(AnnotationType.TABULAR)) {
            TabularAnnotationQuery.createDeleteProjectPreparedStatement(loader);
            String query = TabularAnnotationQuery.getDeleteProjectQuery();
//            String query2 = TabularAnnotationQuery.getGetDeleteProjectAttributeQuery();
//            JDBCPool pool = this.holder.getJDBCPool(loader);
//            return pool.withConnection(conn ->
//                    conn.preparedQuery(query).execute()
//                            .map(DBUtils::toVoid)
//                            .compose(res ->
//                                    conn.preparedQuery(query2).execute(params)).map(DBUtils::toVoid));
            return runQuery(query,holder.getJDBCPool(loader))
                    .map(DBUtils::toVoid);
        }

        return runQuery(AnnotationQuery.getDeleteProject(), params, holder.getJDBCPool(loader))
                .map(DBUtils::toVoid);
    }

    public void createNewProject(@NonNull String projectId)
    {
        ProjectLoader loader = Objects.requireNonNull(projectHandler.getProjectLoader(projectId));

        Tuple params = buildNewProject(loader);

        runQuery(PortfolioDbQuery.getCreateNewProject(), params).onComplete(fetch -> {
                    if (fetch.succeeded())
                    {
                        String annotation = Objects.requireNonNull(
                                AnnotationType.get(loader.getAnnotationType())).name();
                        log.info("Project " + loader.getProjectName() + " of " + annotation.toLowerCase(Locale.ROOT) + " created");
                    }
                    else
                    {
                        log.debug("Create project failed from database");
                    }
                });
    }

    public void loadProject(ProjectLoader loader) {
        //load portfolio table last
        Tuple params = buildNewProject(loader);

        runQuery(PortfolioDbQuery.getCreateNewProject(), params)
                .onComplete(DBUtils.handleEmptyResponse(
                        () -> {
                            log.info("Import project " + loader.getProjectName() + " success!");
                        },
                        cause -> log.info("Failed to import project " + loader.getProjectName() + " from configuration file")
                ));
    }

    public void updateLoaderLabelList(ProjectLoader loader, ProjectVersion project, List<String> newLabelListJson)
    {
        List<String> newLabelList = new ArrayList<>();

        for(String label: newLabelListJson)
        {
            String trimmedLabel = StringHandler.removeEndOfLineChar(label);

            newLabelList.add(trimmedLabel);
        }

        project.setCurrentVersionLabelList(newLabelList);
        loader.setLabelList(newLabelList);
    }

    public void updateFileSystemUuidList(@NonNull String projectID)
    {
        ProjectLoader loader = Objects.requireNonNull(projectHandler.getProjectLoader(projectID));

        List<String> uuidList = loader.getUuidListFromDb();

        ProjectVersion project = loader.getProjectVersion();

        project.setCurrentVersionUuidList(uuidList);

        Tuple updateUuidListBody = Tuple.of(project.getUuidVersionDbFormat(), projectID);

        runQuery(PortfolioDbQuery.getUpdateProject(), updateUuidListBody)
                .onComplete(reply -> {
                    if (!reply.succeeded())
                    {
                        log.info("Update list of uuids to Portfolio Database failed");
                    }
                });
    }

    public void configProjectLoaderFromDb()
    {
        runQuery(PortfolioDbQuery.getRetrieveAllProjects(), this.holder.getPortfolioPool())
                .onComplete(DBUtils.handleResponse(
                        result -> {
                            if (result.size() == 0) {
                                log.info("No projects founds.");
                            } else {
                                for (Row row : result)
                                {
                                    Version currentVersion = new Version(row.getString(7));

                                    ProjectVersion project = PortfolioParser.loadProjectVersion(row.getString(8));     //project_version

                                    project.setCurrentVersion(currentVersion.getVersionUuid());

                                    Map<String, List<String>> uuidDict = ActionOps.getKeyWithArray(row.getString(9));
                                    project.setUuidListDict(uuidDict);                                                      //uuid_project_version

                                    Map<String, List<String>> labelDict = ActionOps.getKeyWithArray(row.getString(10));
                                    project.setLabelListDict(labelDict);                                                    //label_project_version

                                    ProjectLoader loader = ProjectLoader.builder()
                                            .projectId(row.getString(0))                                                   //project_id
                                            .projectName(row.getString(1))                                                 //project_name
                                            .annotationType(row.getInteger(2))                                             //annotation_type
                                            .projectPath(new File(row.getString(3)))                                       //project_path
                                            .projectLoaderStatus(ProjectLoaderStatus.DID_NOT_INITIATED)
                                            .isProjectNew(row.getBoolean(4))                                               //is_new
                                            .isProjectStarred(row.getBoolean(5))                                           //is_starred
                                            .projectInfra(ProjectInfra.get(row.getString(6)))                              //project_infra
                                            .projectVersion(project)                                                            //project_version
                                            .portfolioDB(this)
                                            .annotationDB(annotationDB)
                                            .build();

                                    //load each data points
                                    annotationDB.configProjectLoaderFromDb(loader);
                                    projectHandler.loadProjectLoader(loader);
                                }
                            }
                        },
                        cause -> log.info("Retrieving from portfolio database to project loader failed")
                ));
    }

    public void buildProjectFromCLI()
    {
        // TODO: To build project from cli
    }

    public void getProjectMetadata(@NonNull List<ProjectMetaProperties> result, @NonNull String projectId)
    {
        ProjectLoader loader = Objects.requireNonNull(projectHandler.getProjectLoader(projectId));

        Version currentVersion = loader.getProjectVersion().getCurrentVersion();

        File projectPath = loader.getProjectPath();

        if (!projectPath.exists())
        {
            log.info(String.format("Root path of project [%s] is missing! %s does not exist.", loader.getProjectName(), loader.getProjectPath()));
        }

        List<String> existingDataInDir = ImageHandler.getValidImagesFromFolder(projectPath);

        result.add(ProjectMetaProperties.builder()
                .projectName(loader.getProjectName())
                .projectPath(loader.getProjectPath().getAbsolutePath())
                .isNewParam(loader.getIsProjectNew())
                .isStarredParam(loader.getIsProjectStarred())
                .isLoadedParam(loader.getIsLoadedFrontEndToggle())
                .projectInfraParam(loader.getProjectInfra())
                .createdDateParam(currentVersion.getCreatedDate().toString())
                .lastModifiedDate(currentVersion.getLastModifiedDate().toString())
                .currentVersionParam(currentVersion.getVersionUuid())
                .totalUuidParam(existingDataInDir.size())
                .isRootPathValidParam(projectPath.exists())
                .build()
        );

    }

    public void updateIsNewParam(@NonNull String projectID)
    {
        Tuple params = Tuple.of(Boolean.FALSE, projectID);

        runQuery(PortfolioDbQuery.getUpdateIsNewParam(), params)
                .onComplete(DBUtils.handleEmptyResponse(
                        () -> Objects.requireNonNull(projectHandler.getProjectLoader(projectID)).setIsProjectNew(Boolean.FALSE),
                        cause -> log.info("Update is_new param for project of projectid: " + projectID + " failed")
                ));
    }

    private Tuple buildNewProject(@NonNull ProjectLoader loader)
    {
        //version list
        ProjectVersion project = loader.getProjectVersion();

        return Tuple.of(loader.getProjectId(),              //project_id
                loader.getProjectName(),                    //project_name
                loader.getAnnotationType(),                 //annotation_type
                loader.getProjectPath().getAbsolutePath(),  //project_path
                loader.getIsProjectNew(),                   //is_new
                loader.getIsProjectStarred(),               //is_starred
                loader.getProjectInfra().name(),            //project_infra
                project.getCurrentVersion().getDbFormat(),  //current_version
                project.getDbFormat(),                      //version_list
                project.getUuidVersionDbFormat(),           //uuid_version_list
                project.getLabelVersionDbFormat());         //label_version_list

    }

    public ThumbnailProperties queryData(String projectId, String uuid, @NonNull String annotationKey)
    {
        ProjectLoader loader = Objects.requireNonNull(projectHandler.getProjectLoader(projectId));
        Annotation annotation = loader.getUuidAnnotationDict().get(uuid);
        DataInfoProperties version = annotation.getAnnotationDict().get(loader.getCurrentVersionUuid());
        Map<String, String> imgData = new HashMap<>();
        String dataPath = Paths.get(loader.getProjectPath().getAbsolutePath(), annotation.getImgPath()).toString();

        try
        {
            imgData = ImageHandler.getThumbNail(new File(dataPath));
        }
        catch(Exception e)
        {
            log.debug("Failure in reading image of path " + dataPath, e);
        }

        ThumbnailProperties thmbProps = ThumbnailProperties.builder()
                .message(1)
                .uuidParam(uuid)
                .projectNameParam(loader.getProjectName())
                .imgPathParam(dataPath)
                .imgDepth(Integer.parseInt(imgData.get(ParamConfig.getImgDepth())))
                .imgXParam(version.getImgX())
                .imgYParam(version.getImgY())
                .imgWParam(version.getImgW())
                .imgHParam(version.getImgH())
                .fileSizeParam(annotation.getFileSize())
                .imgOriWParam(Integer.parseInt(imgData.get(ParamConfig.getImgOriWParam())))
                .imgOriHParam(Integer.parseInt(imgData.get(ParamConfig.getImgOriHParam())))
                .imgThumbnailParam(imgData.get(ParamConfig.getBase64Param()))
                .build();

        if(annotationKey.equals(ParamConfig.getBoundingBoxParam())) {
            thmbProps.setBoundingBoxParam(new ArrayList<>(version.getAnnotation()));
        } else if(annotationKey.equals(ParamConfig.getSegmentationParam())) {
            thmbProps.setSegmentationParam(new ArrayList<>(version.getAnnotation()));
        }

        return thmbProps;
    }

    public List<String> deleteProjectDataOnComplete(ProjectLoader loader, List<String> deleteUUIDList,
                                                           List<String> deletedDataPathList) throws IOException {
        List<String> dbUUIDList = loader.getUuidListFromDb();
        if (dbUUIDList.removeAll(deleteUUIDList))
        {
            loader.setUuidListFromDb(dbUUIDList);

            List<String> sanityUUIDList = loader.getSanityUuidList();

            if (sanityUUIDList.removeAll(deleteUUIDList))
            {
                loader.setSanityUuidList(sanityUUIDList);
                FileMover.moveFileToDirectory(loader.getProjectPath().toString(), deletedDataPathList);
            }
            else
            {
                log.info("Error in removing uuid list");
            }

            //update Portfolio Verticle
            updateFileSystemUuidList(loader.getProjectId());

        }

        return loader.getSanityUuidList();
    }

    public ProjectStatisticResponse getProjectStatistic(ProjectLoader projectLoader, AnnotationType type)
            throws ExecutionException, InterruptedException {
        int labeledData = 0;
        int unlabeledData = 0;
        List<LabelNameAndCountProperties> labelPerClassInProject = new ArrayList<>();

        File projectPath = projectLoader.getProjectPath();
        if (!projectPath.exists())
        {
            log.info(String.format("Root path of project [%s] is missing! %s does not exist.",
                    projectLoader.getProjectName(), projectLoader.getProjectPath()));
        }

        if(type == AnnotationType.BOUNDINGBOX || type == AnnotationType.SEGMENTATION) {
            LabelListHandler labelListHandler = new LabelListHandler();
            labelListHandler.getImageLabeledStatus(projectLoader.getUuidAnnotationDict());
            labeledData = labelListHandler.getNumberOfLabeledImage();
            unlabeledData = labelListHandler.getNumberOfUnLabeledImage();
            labelPerClassInProject = labelListHandler.getLabelPerClassInProject(projectLoader.getUuidAnnotationDict(), projectLoader);
        }

        else if(type == AnnotationType.TABULAR) {
            CompletableFuture<List<JsonObject>> future = new CompletableFuture<>();
            getAllTabularData(projectLoader.getProjectId()).onComplete(res -> {
                if(res.succeeded()) {
                    future.complete(res.result());
                }

                else if(res.failed()) {
                    future.completeExceptionally(res.cause());
                }
            });
            List<Map<String, Integer>> list = new ArrayList<>();
            for(JsonObject result : future.get()){
                String labelsListString = result.getString("LABEL");
                if(labelsListString != null) {
                    labeledData++;
                    Map<String, Integer> map = new HashMap<>();
                    JSONArray labelsJsonArray = new JSONArray(labelsListString);
                    for(int i = 0; i < labelsJsonArray.length(); i++) {
                        JSONObject jsonObject = labelsJsonArray.getJSONObject(i);
                        String labelName = jsonObject.getString("labelName");
                        if(map.containsKey(labelName)) {
                            map.put(labelName, map.get(labelName) + 1);
                        } else {
                            map.put(labelName, 1);
                        }
                    }
                    list.add(map);
                } else {
                    unlabeledData++;
                }
            }
            Map<String, Integer> map = list.stream()
                    .flatMap(m -> m.entrySet().stream())
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Integer::sum));

            labelPerClassInProject = map.entrySet().stream()
                    .map(m -> LabelNameAndCountProperties.builder().label(m.getKey()).count(m.getValue()).build())
                    .collect(Collectors.toList());
        }

        return ProjectStatisticResponse.builder()
                .message(ReplyHandler.SUCCESSFUL)
                .numLabeledData(labeledData)
                .numUnLabeledData(unlabeledData)
                .labelPerClassInProject(labelPerClassInProject)
                .build();
    }

    public Future<List<JsonObject>> getAllTabularData(String projectID) {
        ProjectLoader loader = projectHandler.getProjectLoader(projectID);
        JDBCPool pool = this.holder.getJDBCPool(loader);
        String query = TabularAnnotationQuery.getProjectAttributeQuery();
        Tuple params = Tuple.of(projectID);

        return pool.withConnection(conn ->
                conn.preparedQuery(query)
                        .execute(params)
                        .map(res -> {
                            if(res.size() != 0) {
                                Row row = res.iterator().next();
                                TabularAnnotationQuery.createGetAllDataPreparedStatement(loader, row.getString(0));

                                return TabularAnnotationQuery.getGetAllDataQuery();
                            }
                            return null;
                        })
                        .compose(res -> conn.preparedQuery(res).execute())
                        .map(res -> {
                            if (res.size() != 0) {
                                List<JsonObject> list = new ArrayList<>();
                                for(Row row : res.value()) {
                                    list.add(row.toJson());
                                }
                                return list;
                            }
                            return null;
                        })
                )
                .onSuccess(res -> log.info("Retrieve data success"))
                .onFailure(res -> log.info("Fail to retrieve data from database. " + res.getCause().getMessage()));
    }

    public Future<Void> updateTabularLabel(String projectID, UpdateTabularDataBody requestBody) {
        ProjectLoader loader = projectHandler.getProjectLoader(projectID);
        TabularAnnotationQuery.createUpdateDataPreparedStatement(loader);
        String query = TabularAnnotationQuery.getUpdateDataQuery();

        String uuid = requestBody.getUuid();
        String labelList = requestBody.getLabel();

        Tuple params = Tuple.of(labelList, uuid, projectID);
        return runQuery(query, params, this.holder.getJDBCPool(loader))
                .map(DBUtils::toVoid);
    }

    public Future<JsonObject> getTabularDataByUuid(String projectID, String uuid) {
        ProjectLoader loader = projectHandler.getProjectLoader(projectID);
        String projectAttributeQuery = TabularAnnotationQuery.getProjectAttributeQuery();
        JDBCPool pool = this.holder.getJDBCPool(loader);

        Tuple params = Tuple.of(projectID);
        Tuple params2 = Tuple.of(uuid, projectID);

        return pool.withConnection(conn ->
            conn.preparedQuery(projectAttributeQuery)
                    .execute(params)
                    .map(res -> {
                        if(res.size() != 0) {
                            Row row = res.iterator().next();
                            TabularAnnotationQuery.createGetSpecificDataPreparedStatement(loader, row.getString(0));

                            return TabularAnnotationQuery.getGetDataQuery();
                        }
                        return null;
                    })
                    .compose(res -> conn.preparedQuery(res).execute(params2))
                    .map(res -> {
                        if (res.size() != 0) {
                            conn.close();
                            return res.iterator().next().toJson();
                        }
                        conn.close();
                        return null;
                    })
            )
            .onSuccess(res -> log.info("Retrieve data success"))
            .onFailure(res -> log.info("Fail to retrieve data from database. " + res.getCause().getMessage()));

    }

    public Future<JsonObject> getAttributeTypeMap(String projectId) {
        ProjectLoader loader = projectHandler.getProjectLoader(projectId);
        String query = TabularAnnotationQuery.getAttributeTypeMapQuery();
        Tuple params = Tuple.of(projectId);

        return runQuery(query, params, this.holder.getJDBCPool(loader)).map(result -> {
            JsonObject jsonObject;
            if(result.size() != 0) {
                Row row = result.iterator().next();
                jsonObject = new JsonObject(row.getString(0));

                return jsonObject;
            }

            log.info("Fail to retrieve the attribute type map from database");
            return null;
        });
    }

    public Future<JsonArray> getLabel(String projectId, String uuid) {
        ProjectLoader loader = projectHandler.getProjectLoader(projectId);
        TabularAnnotationQuery.createGetLabelPreparedStatement(loader);
        String query = TabularAnnotationQuery.getGetLabelQuery();
        Tuple params = Tuple.of(uuid, projectId);

        return runQuery(query, params, this.holder.getJDBCPool(loader)).map(result -> {
            JsonArray labelsListJsonArray = new JsonArray();
            if(result.size() != 0) {
                Row row = result.iterator().next();
                String labelsJsonString = row.getString(0);

                if(labelsJsonString == null) {
                    JsonObject emptyLabelJson = new JsonObject().put("labelName", "").put("tagColor", "");
                    labelsListJsonArray.add(emptyLabelJson);
                }
                else {
                    JSONArray labelsJsonArray = new JSONArray(labelsJsonString);
                    TabularHandler.processJSONArrayToJsonArray(labelsJsonArray, labelsListJsonArray);
                }
                return labelsListJsonArray;
            }
            log.info("Fail to query label");
            return null;
        });
    }

    public void updateLabel(String projectID, JsonObject requestBody) {
        ProjectLoader loader = projectHandler.getProjectLoader(projectID);
        TabularAnnotationQuery.createUpdateDataPreparedStatement(loader);
        String query = TabularAnnotationQuery.getUpdateDataQuery();

        String uuid = requestBody.getString("uuid");
        String labelList = requestBody.getString("labels");
        Tuple params = Tuple.of(labelList, uuid, projectID);

        runQuery(query, params, this.holder.getJDBCPool(loader))
                .map(DBUtils::toVoid);
    }

    public Future<Void> writeFile(String projectId, String format, boolean isFilterInvalidData) throws InterruptedException, ExecutionException, IOException {
        switch(format) {
            case "csv" -> {
                return writeCsvFile(projectId, isFilterInvalidData);
            }
            case "excel" -> {
                return writeExcelFile(projectId, isFilterInvalidData);
            }
            case "json" -> {
                return writeJsonFile(projectId, isFilterInvalidData);
            }
        }
        return null;
    }

    public Future<Void> writeCsvFile(String projectId, boolean isFilterInvalidData) throws IOException, ExecutionException, InterruptedException {
        ProjectLoader loader = projectHandler.getProjectLoader(projectId);
        String query = TabularAnnotationQuery.getProjectAttributeQuery();
        Tuple params = Tuple.of(projectId);
        JDBCPool pool = this.holder.getJDBCPool(loader);
        List<String> extractedColumnNames = new ArrayList<>();
        Promise<Void> promise = Promise.promise();

        return pool.withConnection(conn -> {
            conn.preparedQuery(query).execute(params).map(res -> {
                if(res.size() != 0) {
                    Row row = res.iterator().next();
                    String columnNames = row.getString(0) + ",fileName,label";
                    String[] columnNamesArray = columnNames.split(",");
                    extractedColumnNames.addAll(Arrays.asList(columnNamesArray));
                    TabularAnnotationQuery.createGetAllDataPreparedStatement(loader, row.getString(0));

                    return TabularAnnotationQuery.getGetAllDataQuery();
                }
                return null;
            }).compose(res -> conn.preparedQuery(res).execute())
            .map(result -> {
                if(result.size() != 0) {
                    return listOfObjectOfTabularData(result.value(), extractedColumnNames, isFilterInvalidData);
                }
                return null;
            }).onComplete(res -> {
                if(res.succeeded()) {
                    try {
                        log.info("Generating csv file...");
                        csvOutputFileWriter(res.result(), projectId);
                        promise.complete();
                    } catch (IOException e) {
                        promise.fail("Error in generating csv file");
                    }
                }

                if(res.failed()) {
                    promise.fail(res.cause());
                }
            });
            return promise.future();
        });

//        CompletableFuture<String> future = new CompletableFuture<>();
//        pool.preparedQuery(query).execute(params).onComplete(res -> {
//            if(res.succeeded()) {
//                future.complete(res.result().iterator().next().getString(0));
//            } else {
//                future.completeExceptionally(res.cause());
//            }
//        });
//
//        String columnNames = future.get() + ",fileName,label";
//        String[] arr = columnNames.split(",");
//        List<String> list1 = Arrays.asList(arr);
//
//        TabularAnnotationQuery.createGetAllDataPreparedStatement(loader, future.get());
//        String query2 = TabularAnnotationQuery.getGetAllDataQuery();
//
//        CompletableFuture<List<Object[]>> future1 = new CompletableFuture<>();
//        pool.preparedQuery(query2).execute().onComplete(res -> {
//            if(res.succeeded()) {
//                List<Object[]> listOfStringArray = new ArrayList<>();
//                listOfStringArray.add(arr);
//                for(Row row : res.result()) {
//                    List<Object> tempList = new ArrayList<>();
//                    for(int i = 0; i < list1.size(); i++) {
//                        Object value = row.toJson().getValue(list1.get(i).toUpperCase());
//                        tempList.add(Objects.requireNonNullElse(value, ""));
//                    }
//                    Object[] result = tempList.toArray(Object[]::new);
//                    listOfStringArray.add(result);
//                    tempList.clear();
//                }
//                future1.complete(listOfStringArray);
//            } else {
//                future1.completeExceptionally(res.cause());
//            }
//        });
//
//        csvOutputFileWriter(future1.get(), projectId);
    }

    private static String extractLabelsFromArrayToString(Object value) {
        JSONArray labelsJsonArray = new JSONArray(value.toString());
        List<String> labelList = new ArrayList<>();
        for(int i = 0; i < labelsJsonArray.length(); i++) {
            labelList.add((String) labelsJsonArray.getJSONObject(i).get("labelName"));
        }
        String labelListString = StringUtils.join(labelList, " ");
        List<String> list = new ArrayList<>();
        list.add(labelListString);
        return list.toString();
    }

    private List<Object[]> listOfObjectOfTabularData(RowSet<Row> result, List<String> extractedColumnNames,
                                                     boolean isFilteredInvalidData)
    {
        List<Object[]> resultArrayList = new ArrayList<>();
        resultArrayList.add(extractedColumnNames.toArray());
        for(Row row : result.value()) {
            List<Object> tempList = new ArrayList<>();
            if(isFilteredInvalidData) {
                if(!checkContainInvalidData(row)) {
                    getListOfTabularObject(extractedColumnNames, resultArrayList, row, tempList);
                }
            } else {
                getListOfTabularObject(extractedColumnNames, resultArrayList, row, tempList);
            }
        }
        return resultArrayList;
    }

    private void getListOfTabularObject(List<String> extractedColumnNames, List<Object[]> resultArrayList, Row row, List<Object> tempList) {
        for (String extractedColumnName : extractedColumnNames) {
            String columnName = extractedColumnName.toUpperCase();
            Object value = row.toJson().getValue(columnName);
            if (columnName.equals("LABEL")) {
                if (value == null) {
                    tempList.add("No Label");
                } else {
                    tempList.add(extractLabelsFromArrayToString(value));
                }
            } else {
                tempList.add(value);
            }
        }
        Object[] resultArray = tempList.toArray(Object[]::new);
        resultArrayList.add(resultArray);
        tempList.clear();
    }

    private boolean checkContainInvalidData(Row row){
        Object value = row.toJson().getValue("LABEL");
        if(value != null) {
            JSONArray jsonArray = new JSONArray(value.toString());
            for(int i = 0; i < jsonArray.length(); i++) {
                String label = jsonArray.getJSONObject(i).getString("labelName");
                if(label.equals("Invalid")) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<Map<String, Object>> mapOfObjectOfTabularData(RowSet<Row> result, List<String> extractedColumnNames,
                                                               boolean isFilterInvalidData)
    {
        List<Map<String, Object>> resultMapList = new ArrayList<>();
        Map<String, Object> tempMap = new LinkedHashMap<>();

        for(String name : extractedColumnNames) {
            tempMap.put(name, name);
        }
        resultMapList.add(tempMap);
        tempMap.clear();

        for(Row row : result.value()) {
            if(isFilterInvalidData) {
                if(checkContainInvalidData(row)) {
                    getMapOfTabularObject(extractedColumnNames, resultMapList, tempMap, row);
                }
            } else {
                getMapOfTabularObject(extractedColumnNames, resultMapList, tempMap, row);
            }
        }
        return resultMapList;
    }

    private void getMapOfTabularObject(List<String> extractedColumnNames, List<Map<String, Object>> resultMapList,
                                       Map<String, Object> tempMap, Row row) {
        for (String extractedColumnName : extractedColumnNames) {
            String columnName = extractedColumnName.toUpperCase();
            Object value = row.toJson().getValue(columnName);
            if (columnName.equals("LABEL")) {
                if (value == null) {
                    tempMap.put(columnName,"No Label");
                } else {
                    tempMap.put(columnName, extractLabelsFromArrayToString(value));
                }
            } else {
                tempMap.put(columnName, value);
            }
        }
        resultMapList.add(tempMap);
        tempMap.clear();
    }

    private List<JsonObject> lisOfJsonObjectOfTabularData(RowSet<Row> result, List<String> extractedColumnNames, boolean isFilterInvalidData) {
        List<JsonObject> resultList = new ArrayList<>();

        for(Row row : result.value()) {
            JsonObject jsonObject = new JsonObject();
            if(isFilterInvalidData) {
                if(checkContainInvalidData(row)){
                    getTabularJsonObject(extractedColumnNames, resultList, row, jsonObject);
                }
            } else {
                getTabularJsonObject(extractedColumnNames, resultList, row, jsonObject);
            }

        }

        return resultList;
    }

    private void getTabularJsonObject(List<String> extractedColumnNames, List<JsonObject> resultList, Row row, JsonObject jsonObject) {
        for (String extractedColumnName : extractedColumnNames) {
            String columnName = extractedColumnName.toUpperCase();
            Object value = row.toJson().getValue(columnName);
            if (columnName.equals("LABEL")) {
                if (value == null) {
                    jsonObject.put(extractedColumnName,"No Label");
                } else {
                    jsonObject.put(extractedColumnName, extractLabelsFromArrayToString(value));
                }
            } else {
                jsonObject.put(extractedColumnName, value);
            }
        }
        resultList.add(jsonObject);
    }

    private void csvOutputFileWriter(List<Object[]> retrievedDataList, String projectId) throws IOException {
        ProjectLoader loader = projectHandler.getProjectLoader(projectId);
        String projectPath = loader.getProjectPath().getAbsolutePath();
        File projectDirectory = new File(projectPath);
        String tempFilePath = loader.getProjectPath() + File.separator + loader.getProjectName() + ".text";
        String csvFilePath = loader.getProjectPath() + File.separator + loader.getProjectName() + ".csv";
        File csvFile = new File(csvFilePath);
        File tempFile = new File(tempFilePath);
        BufferedWriter writer = new BufferedWriter(new FileWriter(csvFile));

        if (projectDirectory.mkdirs()) {
            log.info("Project folder " + projectDirectory.getName() + " is created");
        } else {
            log.info("Project folder " + projectDirectory.getName() + " is exist");
        }

        for (Object[] objArr : retrievedDataList) {
            for (int i = 0; i < objArr.length; i++) {
                if (i != objArr.length - 1) {
                    writer.write(objArr[i].toString());
                    writer.write(",");
                } else {
                    writer.write(objArr[i].toString());
                }
            }
            writer.newLine();
        }
        writer.close();
        tempFile.renameTo(csvFile);

        if (csvFile.exists()) {
            log.info(csvFile.getName() + " is generated in project folder");
        } else {
            log.info("Fail to generate csv file for project " + loader.getProjectName());
        }
    }

    private Future<Void> writeExcelFile(String projectId, boolean isFilterInvalidData) throws ExecutionException, InterruptedException {
        ProjectLoader loader = projectHandler.getProjectLoader(projectId);
        String query = TabularAnnotationQuery.getProjectAttributeQuery();
        Tuple params = Tuple.of(projectId);
        JDBCPool pool = this.holder.getJDBCPool(loader);
        List<String> extractedColumnNames = new ArrayList<>();
        Promise<Void> promise = Promise.promise();

        return pool.withConnection(conn -> {
            conn.preparedQuery(query).execute(params).map(res -> {
                if(res.size() != 0) {
                    Row row = res.iterator().next();
                    String columnNames = row.getString(0) + ",fileName,label";
                    String[] columnNamesArray = columnNames.split(",");
                    extractedColumnNames.addAll(Arrays.asList(columnNamesArray));
                    TabularAnnotationQuery.createGetAllDataPreparedStatement(loader, row.getString(0));

                    return TabularAnnotationQuery.getGetAllDataQuery();
                }
                return null;
            }).compose(res -> conn.preparedQuery(res).execute())
                    .map(result -> {
                        if(result.size() != 0) {
                            return mapOfObjectOfTabularData(result.value(), extractedColumnNames, isFilterInvalidData);
                        }
                        return null;
                    })
            .onComplete(res -> {
                if(res.succeeded()) {
                    try {
                        log.info("Generating excel file...");
                        excelOutputFileWriter(res.result(), projectId);
                        promise.complete();
                    } catch (ExecutionException | InterruptedException | FileNotFoundException e) {
                        promise.fail("Error in generating excel file");
                    }
                }
                if(res.failed()) {
                    promise.fail(res.cause());
                }
            });
            return promise.future();
        });
    }

    private void excelOutputFileWriter(List<Map<String, Object>> retrievedDataList, String projectId)
            throws ExecutionException, InterruptedException, FileNotFoundException {
        ProjectLoader loader = projectHandler.getProjectLoader(projectId);
        String projectPath = loader.getProjectPath().getAbsolutePath();
        File projectDirectory = new File(projectPath);
        String excelFilePath = loader.getProjectPath() + File.separator + loader.getProjectName() + ".xlsx";
        XSSFWorkbook excelWorkbook = new XSSFWorkbook();
        XSSFSheet excelSpreadSheet = excelWorkbook.createSheet(loader.getProjectName() + "_data");
        JsonObject attributeMap;

        if(projectDirectory.mkdirs()) {
            log.info("Project folder " + projectDirectory.getName() + " is created");
        } else {
            log.info("Project folder " + projectDirectory.getName() + " is exist");
        }

        CompletableFuture<JsonObject> future = new CompletableFuture<>();
        getAttributeTypeMap(projectId)
                .onComplete(res -> {
                    if(res.succeeded()) {
                        future.complete(res.result());
                    } else {
                        future.completeExceptionally(res.cause());
                    }
                });
        attributeMap = future.get();

        for(int i = 0; i < retrievedDataList.size(); i++) {
            XSSFRow row = excelSpreadSheet.createRow(i++);
            Map<String, Object> data = retrievedDataList.get(i);
            int columnCount = 0;
            for(Map.Entry<String, Object> entry : data.entrySet()) {
                Cell cell = row.createCell(columnCount++);
                String attributeType = attributeMap.getString(entry.getKey());
                String dataType = TabularHandler.checkAttributeType(attributeType);
                switch (dataType) {
                    case "Integer" -> cell.setCellValue((Integer) entry.getValue());
                    case "Double" -> cell.setCellValue((Double) entry.getValue());
                    case "String" -> cell.setCellValue((String) entry.getValue());
                    case "Date" -> cell.setCellValue((Date) entry.getValue());
                }
            }
        }

        try (FileOutputStream outputStream = new FileOutputStream(excelFilePath)) {
            excelWorkbook.write(outputStream);
            log.info(loader.getProjectName() + ".xlsx" + " is generated in project folder");
        } catch (IOException e) {
            log.info("Fail to generate excel file for project " + loader.getProjectName());
        }
    }

    private Future<Void> writeJsonFile(String projectId, boolean isFilterInvalidData) {
        ProjectLoader loader = projectHandler.getProjectLoader(projectId);
        String query = TabularAnnotationQuery.getProjectAttributeQuery();
        Tuple params = Tuple.of(projectId);
        JDBCPool pool = this.holder.getJDBCPool(loader);
        List<String> extractedColumnNames = new ArrayList<>();
        Promise<Void> promise = Promise.promise();

        return pool.withConnection(conn -> {
            conn.preparedQuery(query).execute(params).map(res -> {
                if(res.size() != 0) {
                    Row row = res.iterator().next();
                    String columnNames = row.getString(0) + ",fileName,label";
                    String[] columnNamesArray = columnNames.split(",");
                    extractedColumnNames.addAll(Arrays.asList(columnNamesArray));
                    TabularAnnotationQuery.createGetAllDataPreparedStatement(loader, row.getString(0));

                    return TabularAnnotationQuery.getGetAllDataQuery();
                }
                return null;
            }).compose(res -> conn.preparedQuery(res).execute())
                    .map(result -> {
                        if(result.size() != 0) {
                            return lisOfJsonObjectOfTabularData(result.value(), extractedColumnNames, isFilterInvalidData);
                        }
                        return null;
                    })
                    .onComplete(res -> {
                        if(res.succeeded()) {
                            try {
                                log.info("Generating Json file...");
                                jsonFileWriter(res.result(), projectId);
                                promise.complete();
                            } catch (IOException e) {
                                promise.fail("Error in generating json file");
                            }
                        }

                        if(res.failed()) {
                            promise.fail(res.cause());
                        }
                    });
            return promise.future();
        });
    }

    private void jsonFileWriter(List<JsonObject> result, String projectId) throws IOException {
        ProjectLoader loader = projectHandler.getProjectLoader(projectId);
        String projectPath = loader.getProjectPath().getAbsolutePath();
        File projectDirectory = new File(projectPath);
        String jsonFilePath = loader.getProjectPath() + File.separator + loader.getProjectName() + ".json";
        FileWriter writer = new FileWriter(jsonFilePath);

        if(projectDirectory.mkdirs()) {
            log.info("Project folder " + projectDirectory.getName() + " is created");
        } else {
            log.info("Project folder " + projectDirectory.getName() + " is exist");
        }

        try {
            writer.write(result.toString());
            log.info(loader.getProjectName() + ".json is generated in project folder");
        }
        catch (IOException e) {
            log.info("Fail to generate json file for project " + loader.getProjectName());
        }
        finally {
            writer.flush();
            writer.close();
        }
    }

//    public Future<JsonObject> automateTabularLabelling(String projectId, JsonObject preLabellingConditions,
//                                                       String currentUuid, PortfolioDB portfolioDB) throws Exception {
//        ProjectLoader loader = projectHandler.getProjectLoader(projectId);
//        List<String> uuidList = loader.getUuidListFromDb();
//        TabularHandler tabularHandler = new TabularHandler();
//        tabularHandler.initiateAutomaticLabellingForTabular(projectId, preLabellingConditions, uuidList, portfolioDB);
//        log.info("initiate");
//        Promise<JsonObject> promise = Promise.promise();
//
//        if(tabularHandler.checkIsCurrentUuidFinished(currentUuid)) {
//            getTabularDataByUuid(projectId, currentUuid).onComplete(res -> {
//                if(res.succeeded()) {
//                    promise.complete(res.result());
//                }
//
//                if(res.failed()) {
//                    promise.fail(res.cause());
//                }
//            });
//        }
//        return promise.future();
//    }

    public Future<List<String>> getAllInvalidData(String projectID) {
        ProjectLoader loader = projectHandler.getProjectLoader(projectID);
        JDBCPool pool = this.holder.getJDBCPool(loader);
        String query = TabularAnnotationQuery.getProjectAttributeQuery();
        Tuple params = Tuple.of(projectID);

        return pool.withConnection(conn ->
                conn.preparedQuery(query)
                        .execute(params)
                        .map(res -> {
                            if(res.size() != 0) {
                                Row row = res.iterator().next();
                                TabularAnnotationQuery.createGetAllDataPreparedStatement(loader, row.getString(0));

                                return TabularAnnotationQuery.getGetAllDataQuery();
                            }
                            return null;
                        })
                        .compose(res -> conn.preparedQuery(res).execute())
                        .map(res -> {
                            if (res.size() != 0) {
                                List<String> listOfInvalidUUID = new ArrayList<>();
                                for(Row row : res.value()) {
                                    boolean invalidLabels = false;
                                    JsonObject resultObject = row.toJson();
                                    String labelsJsonString = resultObject.getString("LABEL");
                                    if(labelsJsonString != null) {
                                        JSONArray labelsJsonArray = new JSONArray(labelsJsonString);
                                        for(int i = 0; i < labelsJsonArray.length(); i++) {
                                            JSONObject object = labelsJsonArray.getJSONObject(i);
                                            if(object.getString("labelName").equals("Invalid")) {
                                                invalidLabels = true;
                                            }
                                        }
                                        if(invalidLabels) {
                                            listOfInvalidUUID.add(resultObject.getString("UUID"));
                                        }
                                    }
                                }
                                return listOfInvalidUUID;
                            }
                            return null;
                        })
        )
                .onSuccess(res -> log.info("Retrieve data success"))
                .onFailure(res -> log.info("Fail to retrieve data from database. " + res.getCause().getMessage()));
    }

}
