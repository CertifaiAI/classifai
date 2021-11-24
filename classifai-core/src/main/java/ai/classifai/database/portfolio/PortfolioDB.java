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

import ai.classifai.action.*;
import ai.classifai.action.parser.PortfolioParser;
import ai.classifai.action.rename.RenameDataErrorCode;
import ai.classifai.action.rename.RenameProjectData;
import ai.classifai.database.DBUtils;
import ai.classifai.database.JDBCPoolHolder;
import ai.classifai.database.annotation.AnnotationDB;
import ai.classifai.database.annotation.AnnotationQuery;
import ai.classifai.database.versioning.Annotation;
import ai.classifai.database.versioning.ProjectVersion;
import ai.classifai.database.versioning.Version;
import ai.classifai.dto.data.DataInfoProperties;
import ai.classifai.dto.data.ProjectConfigProperties;
import ai.classifai.dto.data.ProjectMetaProperties;
import ai.classifai.dto.data.ThumbnailProperties;
import ai.classifai.loader.NameGenerator;
import ai.classifai.loader.ProjectLoader;
import ai.classifai.loader.ProjectLoaderStatus;
import ai.classifai.ui.enums.FileSystemStatus;
import ai.classifai.util.ParamConfig;
import ai.classifai.util.collection.UuidGenerator;
import ai.classifai.util.data.FileHandler;
import ai.classifai.util.data.ImageHandler;
import ai.classifai.util.data.StringHandler;
import ai.classifai.util.message.ReplyHandler;
import ai.classifai.util.project.ProjectHandler;
import ai.classifai.util.project.ProjectInfra;
import ai.classifai.util.type.AnnotationType;
import ai.classifai.wasabis3.WasabiImageHandler;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

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
    private final ProjectImport projectImport;


    public PortfolioDB(JDBCPoolHolder holder, ProjectHandler projectHandler, ProjectExport projectExport,
                       AnnotationDB annotationDB, ProjectImport projectImport) {
        this.holder = holder;

        this.projectHandler = projectHandler;
        this.projectExport = projectExport;
        this.annotationDB = annotationDB;
        this.projectImport = projectImport;
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

        for (int i = 0; i < oriUUIDList.size(); ++i) {
            final Integer currentLength = i + 1;
            final String UUID = oriUUIDList.get(i);
            Tuple params = Tuple.of(projectId, UUID);

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
                        String imageStr;
                        Row row = result.iterator().next();
                        String dataPath = row.getString(0);

                        if(loader.isCloud()) {
                            imageStr = WasabiImageHandler.encodeFileToBase64Binary(loader.getWasabiProject(), dataPath);
                        } else {
                            File fileImgPath = loader.getDataFullPath(dataPath);
                            imageStr = ImageHandler.encodeFileToBase64Binary(fileImgPath);
                        }
                        return imageStr;
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
        Tuple params = Tuple.of(projectId);

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
        try
        {
            // To build project from cli
            String projectName = projectHandler.getCliProjectInitiator().getProjectName();
            AnnotationType annotationType = projectHandler.getCliProjectInitiator().getProjectType();
            File dataPath = projectHandler.getCliProjectInitiator().getRootDataPath();
            Boolean isDocker = ParamConfig.isDockerEnv();

            // load label list file into project
            File labelPath = projectHandler.getCliProjectInitiator().getLabelFilePath();
            ArrayList<String> nameList = new ArrayList<>();

            runQuery(PortfolioDbQuery.getRetrieveAllProjectsForAnnotationType(), this.holder.getPortfolioPool())
                    .onComplete(DBUtils.handleResponse(
                        result -> {
                            if (result.size() == 0) {
                                log.info("No projects founds.");
                            }
                            else {
                                for (Row row : result) {
                                    nameList.add(row.getString(0));
                                }

                                if (nameList.contains(projectName)) {
                                    log.info("Project name exist in database, please use another name");
                                    System.exit(0);
                                } else {
                                    if (labelPath == null) {
                                        ProjectLoader loaderWithoutLabel = ProjectLoader.builder()
                                                .projectId(UuidGenerator.generateUuid())
                                                .projectName(projectName)
                                                .annotationType(annotationType.ordinal())
                                                .projectPath(dataPath)
                                                .projectLoaderStatus(ProjectLoaderStatus.LOADED)
                                                .projectInfra(ProjectInfra.ON_PREMISE)
                                                .fileSystemStatus(FileSystemStatus.ITERATING_FOLDER)
                                                .isDocker(isDocker)
                                                .build();

                                        projectHandler.checkCLIBuildProjectStatus(loaderWithoutLabel);
                                    } else {
                                        List<String> labelList = new LabelListImport(labelPath).getValidLabelList();

                                        ProjectLoader loaderWithLabel = ProjectLoader.builder()
                                                .projectId(UuidGenerator.generateUuid())
                                                .projectName(projectName)
                                                .annotationType(annotationType.ordinal())
                                                .projectPath(dataPath)
                                                .labelList(labelList)
                                                .projectLoaderStatus(ProjectLoaderStatus.LOADED)
                                                .projectInfra(ProjectInfra.ON_PREMISE)
                                                .fileSystemStatus(FileSystemStatus.ITERATING_FOLDER)
                                                .isDocker(isDocker)
                                                .build();

                                        projectHandler.checkCLIBuildProjectStatus(loaderWithLabel);
                                    }
                                }
                            }
                        },
                        cause -> log.info("Build project using command line failed")
                    ));
        }
        catch (NullPointerException e)
        {
            log.debug("Build project using command line interface not initiated");
        }
    }

    public void importProjectFromCLI()
    {
        try
        {
            // Load configuration file using CLI
            File projectConfigFile = projectHandler.getCliProjectImporter().getConfigFilePath();
            FileHandler.checkProjectConfigExtension(projectConfigFile.toString());

            ActionConfig.setJsonFilePath(Paths.get(FilenameUtils.getFullPath(projectConfigFile.toString())).toString());
            String jsonStr = IOUtils.toString(new FileReader(projectConfigFile));
            ProjectConfigProperties importConfig = projectImport.jsonStrToConfig(jsonStr);

            String projectName = importConfig.getProjectName();
            String projectId = importConfig.getProjectID();
            String projectPath = importConfig.getProjectPath();
            Map<String, String> project = new HashMap<>();

            runQuery(PortfolioDbQuery.getRetrieveAllProjects(), this.holder.getPortfolioPool())
                    .onComplete(DBUtils.handleResponse(
                        result -> {
                            if (result.size() == 0) {
                                log.info("No projects founds.");
                            }
                            else {
                                for (Row row : result) {
                                    project.put(row.getString(1), row.getString(3));
                                }

                                // If database dont have the project name, load the project configuration file
                                if (!project.containsKey(projectName)) {
                                    projectImport.loadProjectFromImportingConfigFile(importConfig);
                                    log.info("Project " + projectName + " loaded from configuration file");
                                } else {
                                    // If the project to be loaded has the same name and path with a project in database, reload the project
                                    if (project.get(projectName).equals(projectPath)) {
                                        projectHandler.checkReloadProjectFromDatabaseStatus(projectId);
                                    } else {
                                        // If the project to be loaded has same name with a project in database but it has different path, load project with new generated name
                                        importConfig.setProjectID(UuidGenerator.generateUuid());
                                        importConfig.setProjectName(new NameGenerator().getNewProjectName());
                                        projectImport.loadProjectFromImportingConfigFile(importConfig);
                                        log.info("Project loaded with new generated name " + importConfig.getProjectName());

                                        // handle old project configuration file
                                        String originalConfigFilePath = importConfig.getProjectPath();
                                        String deletedConfigFolderName = Paths.get(originalConfigFilePath, ParamConfig.getDeleteDataFolderName()).toString();
                                        String projectConfigName = Paths.get(projectConfigFile.toString()).getFileName().toString();


                                        File folderName = new File(deletedConfigFolderName);
                                        Path source = Paths.get(projectConfigFile.toString());
                                        Path target = Paths.get(deletedConfigFolderName, projectConfigName);

                                        FileMover.moveConfigFile(folderName, source, target);
                                    }
                                }
                            }
                        },
                        cause -> log.info("Import project using command line failed")
                    ));
        }
        catch (NullPointerException | IOException e)
        {
            log.debug("Import project using command line interface not initiated");
        }
    }

    public void getProjectMetadata(@NonNull List<ProjectMetaProperties> result, @NonNull String projectId)
    {
        ProjectLoader loader = Objects.requireNonNull(projectHandler.getProjectLoader(projectId));

        Version currentVersion = loader.getProjectVersion().getCurrentVersion();

        File projectPath = loader.getProjectPath();

        Boolean isDocker = loader.getIsDocker();

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
                .isCloud(loader.isCloud())
                .projectInfraParam(loader.getProjectInfra())
                .createdDateParam(currentVersion.getCreatedDate().toString())
                .lastModifiedDate(currentVersion.getLastModifiedDate().toString())
                .currentVersionParam(currentVersion.getVersionUuid())
                .totalUuidParam(existingDataInDir.size())
                .isRootPathValidParam(projectPath.exists())
                .isDocker(isDocker)
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
                loader.getIsDocker(),                       //is_docker
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
        String dataPath = "";

        if(loader.isCloud())
        {
            try
            {
                BufferedImage img = WasabiImageHandler.getThumbNail(loader.getWasabiProject(), annotation.getImgPath());

                //not checking orientation for on cloud version
                imgData = ImageHandler.getThumbNail(img);
            }
            catch(Exception e)
            {
                log.debug("Unable to write Buffered Image.");
            }

        }
        else
        {
            dataPath = Paths.get(loader.getProjectPath().getAbsolutePath(), annotation.getImgPath()).toString();

            try
            {
                Mat imageMat  = Imgcodecs.imread(dataPath);

                BufferedImage img = ImageHandler.toBufferedImage(imageMat);

                imgData = ImageHandler.getThumbNail(img);
            }
            catch(Exception e)
            {
                log.debug("Failure in reading image of path " + dataPath, e);
            }
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
}
