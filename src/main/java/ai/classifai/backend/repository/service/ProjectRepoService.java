package ai.classifai.backend.repository.service;

import ai.classifai.backend.repository.JDBCPoolHolder;
import ai.classifai.backend.repository.database.DBUtils;
import ai.classifai.backend.repository.query.PortfolioDbQuery;
import ai.classifai.backend.repository.query.QueryOps;
import ai.classifai.backend.repository.query.TabularAnnotationQuery;
import ai.classifai.backend.utility.action.FileMover;
import ai.classifai.core.data.handler.ImageHandler;
import ai.classifai.core.dto.ProjectDTO;
import ai.classifai.core.entity.project.Project;
import ai.classifai.core.enumeration.AnnotationType;
import ai.classifai.core.enumeration.ProjectInfra;
import ai.classifai.core.loader.ProjectHandler;
import ai.classifai.core.loader.ProjectLoader;
import ai.classifai.core.loader.ProjectLoaderStatus;
import ai.classifai.core.service.project.ProjectRepository;
import ai.classifai.core.utility.ActionOps;
import ai.classifai.core.utility.handler.StringHandler;
import ai.classifai.core.utility.parser.PortfolioParser;
import ai.classifai.core.versioning.ProjectVersion;
import ai.classifai.core.versioning.Version;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Slf4j
public class ProjectRepoService implements ProjectRepository {
    private final JDBCPool portfolioPool;
    private final QueryOps queryOps = new QueryOps();
    private final ProjectHandler projectHandler;

    public ProjectRepoService(JDBCPoolHolder jdbcHolder, ProjectHandler projectHandler) {
        this.portfolioPool = jdbcHolder.getPortfolioPool();
        this.projectHandler = projectHandler;
    }

    @Override
    public Future<Project> createProject(@NonNull ProjectDTO projectDTO) {
        ProjectLoader loader = projectHandler.getProjectLoader(projectDTO.getProjectId());
        Tuple params = buildNewProject(loader);

        return queryOps.runQuery(PortfolioDbQuery.getCreateNewProject(), params, portfolioPool)
                .map(response -> {
                    log.info("Project " + projectDTO.getProjectName() + " created");
                    return toProjectEntity(loader);
                });
    }

    @Override
    public Future<Optional<Project>> getProjectById(@NonNull String projectId, @NonNull Integer annotationType) {
        Tuple params = Tuple.of(annotationType, projectId);
        return queryOps.runQuery(PortfolioDbQuery.getRetrieveProjectByIdForAnnotationType(), params, portfolioPool)
                .map(res -> {
                    if (res.size() != 0) {
                        Optional<Project> project = Optional.empty();
                        for (Row row : res) {
                            project = Optional.of(toProjectEntity(row));
                        }
                        return project;
                    }
                    return Optional.empty();
                });
    }

    @Override
    public Future<Project> updateProject(@NonNull Project projectEntity, @NonNull ProjectDTO projectDTO) {
        ProjectLoader loader = projectHandler.getProjectLoader(projectDTO.getProjectId());
        Tuple params = buildNewProject(loader);
        return queryOps.runQuery(PortfolioDbQuery.getUpdateProject(), params, portfolioPool)
                .map(res -> projectEntity);
    }

    @Override
    public Future<Void> deleteProjectById(@NonNull Project projectEntity) {
        Tuple params = Tuple.of(projectEntity.getProjectId());
        return queryOps.runQuery(PortfolioDbQuery.getDeleteProject(), params, portfolioPool)
                .map(res -> {
                    log.info("Delete project " + projectEntity.getProjectName());
                    return DBUtils.toVoid(null);
                })
                .onFailure(res -> log.info(res.getCause().getMessage()));
    }

    @Override
    public Future<Void> renameProject() {
        return null;
    }

    private Project toProjectEntity(@NonNull ProjectLoader loader) {
        ProjectVersion projectVersion = loader.getProjectVersion();

        return Project.builder()
                .projectId(loader.getProjectId())
                .projectName(loader.getProjectName())
                .annotationType(loader.getAnnotationType())
                .projectPath(loader.getProjectPath().getAbsolutePath())
                .isNew(loader.getIsProjectNew())
                .isStarred(loader.getIsProjectStarred())
                .projectInfra(loader.getProjectInfra().name())
                .currentVersion(projectVersion.getCurrentVersion().getDbFormat())
                .uuidProjectVersion(projectVersion.getUuidVersionDbFormat())
                .labelProjectVersion(projectVersion.getLabelVersionDbFormat())
                .versionList(projectVersion.getDbFormat())
                .build();
    }

    private Project toProjectEntity(Row row) {

        return Project.builder()
                .projectId(row.getString(0))
                .projectName(row.getString(1))
                .annotationType(row.getInteger(2))
                .projectPath(row.getString(3))
                .projectFilePath(row.getString(4))
                .isNew(row.getBoolean(5))
                .isStarred(row.getBoolean(6))
                .projectInfra(row.getString(7))
                .currentVersion(row.getString(8))
                .versionList(row.getString(9))
                .uuidProjectVersion(row.getString(10))
                .labelProjectVersion(row.getString(11))
                .build();
    }

    private Tuple buildNewProject(@NonNull ProjectLoader loader)
    {
        //version list
        ProjectVersion project = loader.getProjectVersion();

        return Tuple.of(loader.getProjectId(),              //project_id
                loader.getProjectName(),                    //project_name
                loader.getAnnotationType(),                 //annotation_type
                loader.getProjectPath().getAbsolutePath(),  //project_path
                loader.getProjectFilePath().getAbsolutePath(), //project_file_path
                loader.getIsProjectNew(),                   //is_new
                loader.getIsProjectStarred(),               //is_starred
                loader.getProjectInfra().name(),            //project_infra
                project.getCurrentVersion().getDbFormat(),  //current_version
                project.getDbFormat(),                      //version_list
                project.getUuidVersionDbFormat(),           //uuid_version_list
                project.getLabelVersionDbFormat());         //label_version_list

    }

    public Future<Void> renameProject(String projectId, String newProjectName) {
        ProjectLoader loader = projectHandler.getProjectLoader(projectId);
        AnnotationType type = AnnotationType.get(loader.getAnnotationType());

        if(type == AnnotationType.TABULAR) {
            TabularAnnotationQuery.createChangeProjectTableNamePreparedStatement(loader.getProjectName(), newProjectName);
            String query = TabularAnnotationQuery.getChangeProjectTableNameQuery();
            queryOps.runQuery(query, portfolioPool)
                    .map(DBUtils::toVoid);
        }

        Tuple params = Tuple.of(newProjectName, projectId);

        return queryOps.runQuery(PortfolioDbQuery.getRenameProject(), params, portfolioPool)
                .map(DBUtils::toVoid);
    }

//
//    public Future<Void> reloadProject(String projectId) {
//
//        ProjectLoader loader = Objects.requireNonNull(projectHandler.getProjectLoader(projectId));
//        Promise<Void> promise = Promise.promise();
//
//        if(ImageHandler.loadProjectRootPath(loader, annotationDB)) {
//            promise.complete();
//        } else {
//            promise.fail(ReplyHandler.getFailedReply().toString());
//        }
//
//        return promise.future();
//    }

    public Future<List<Project>> getProjectMetadata(String projectId) {

        Promise<List<Project>> promise = Promise.promise();
        List<Project> result = new ArrayList<>();

        getProjectMetadata(result, projectId);
        promise.complete(result);

        return promise.future();
    }

    public void getProjectMetadata(@NonNull List<Project> result, @NonNull String projectId)
    {
        ProjectLoader loader = Objects.requireNonNull(projectHandler.getProjectLoader(projectId));

        Version currentVersion = loader.getProjectVersion().getCurrentVersion();

        File projectPath = loader.getProjectPath();

        if (!projectPath.exists())
        {
            log.info(String.format("Root path of project [%s] is missing! %s does not exist.", loader.getProjectName(), loader.getProjectPath()));
        }

        List<String> existingDataInDir = ImageHandler.getValidImagesFromFolder(projectPath);

        result.add(Project.builder()
                .projectName(loader.getProjectName())
                .projectPath(loader.getProjectPath().getAbsolutePath())
                .isNew(loader.getIsProjectNew())
                .isStarred(loader.getIsProjectStarred())
                .isLoaded(loader.getIsLoadedFrontEndToggle())
                .projectInfra(loader.getProjectInfra().name())
                .createdDate(currentVersion.getCreatedDate().toString())
                .lastModifiedDate(currentVersion.getLastModifiedDate().toString())
                .currentVersion(currentVersion.getVersionUuid())
                .totalUuid(existingDataInDir.size())
                .isRootPathValid(projectPath.exists())
                .build()
        );

    }

    @Override
    public Future<Optional<List<Project>>> listProjects(@NonNull Integer annotationType) {
        Tuple params = Tuple.of(annotationType);
        return queryOps.runQuery(PortfolioDbQuery.getRetrieveAllProjectsForAnnotationType(), params, portfolioPool)
                .map(result -> {
                    if (result.size() != 0) {
                        List<Project> projectData = new ArrayList<>();
                        for (Row row : result)
                        {
                            String projectName = row.getString(0);
                            getProjectMetadata(projectData, projectHandler.getProjectId(projectName, annotationType));
                        }
                        return Optional.of(projectData);
                    }
                    return Optional.empty();
                });
    }

    @Override
    public Future<Void> updateLastModifiedDate(String projectId, String dbFormat) {
        Tuple params = Tuple.of(dbFormat, projectId);

        return queryOps.runQuery(PortfolioDbQuery.getUpdateLastModifiedDate(), params, portfolioPool)
                .map(DBUtils::toVoid);
    }

    @Override
    public Future<Void> updateLabels(@NonNull String projectId, @NonNull List<String> labelList) {
        ProjectLoader loader = Objects.requireNonNull(projectHandler.getProjectLoader(projectId));
        ProjectVersion project = loader.getProjectVersion();

        updateLoaderLabelList(loader, project, labelList);

        Tuple params = Tuple.of(project.getLabelVersionDbFormat(), projectId);

        return queryOps.runQuery(PortfolioDbQuery.getUpdateLabelList(), params, portfolioPool)
                .map(DBUtils::toVoid);
    }

    public Future<Void> deleteProjectFromPortfolioDb(String projectID) {
        Tuple params = Tuple.of(projectID);

        return queryOps.runQuery(PortfolioDbQuery.getDeleteProject(), params, portfolioPool)
                .map(DBUtils::toVoid);
    }

//    public Future<Void> deleteProjectFromAnnotationDb(String projectId) {
//        ProjectLoader loader = Objects.requireNonNull(projectHandler.getProjectLoader(projectId));
//        AnnotationType annotationType = AnnotationType.get(loader.getAnnotationType());
//        Tuple params = Tuple.of(projectId);
//
//        if (Objects.requireNonNull(annotationType).equals(AnnotationType.TABULAR)) {
//            TabularAnnotationQuery.createDeleteProjectPreparedStatement(loader);
//            String query = TabularAnnotationQuery.getDeleteProjectQuery();
//            String query2 = TabularAnnotationQuery.getGetDeleteProjectAttributeQuery();
//            JDBCPool pool = this.holder.getJDBCPool(loader);
//            return pool.withConnection(conn ->
//                    conn.preparedQuery(query).execute()
//                            .map(DBUtils::toVoid)
//                            .compose(res ->
//                                    conn.preparedQuery(query2).execute(params)).map(DBUtils::toVoid));
//            return queryOps.runQuery(query,holder.getJDBCPool(loader))
//                    .map(DBUtils::toVoid);
//        }
//
//        return queryOps.runQuery(AnnotationQuery.getDeleteProject(), params, holder.getJDBCPool(loader))
//                .map(DBUtils::toVoid);
//    }

//    public void loadProject(@NonNull ProjectLoader loader) {
//        //load portfolio table last
//        Tuple params = buildNewProject(loader);
//
//        queryOps.runQuery(PortfolioDbQuery.getCreateNewProject(), params, portfolioPool)
//                .onComplete(DBUtils.handleEmptyResponse(
//                        () -> {
//                            log.info("Import project " + loader.getProjectName() + " success!");
//                        },
//                        cause -> log.info("Failed to import project " + loader.getProjectName() + " from configuration file")
//                ));
//    }

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

        queryOps.runQuery(PortfolioDbQuery.getUpdateProject(), updateUuidListBody, portfolioPool)
                .onComplete(reply -> {
                    if (!reply.succeeded())
                    {
                        log.info("Update list of uuids to Portfolio Database failed");
                    }
                });
    }

    @Override
    public Future<Map<Integer, List<ProjectLoader>>> configProjectLoaderFromDb()
    {
        Promise<Map<Integer, List<ProjectLoader>>> promise = Promise.promise();

        queryOps.runQuery(PortfolioDbQuery.getRetrieveAllProjects(), portfolioPool)
                .onComplete(DBUtils.handleResponse(
                        result -> {
                            if (result.size() == 0) {
                                log.info("No projects founds.");
                            } else {
                                Map<Integer, List<ProjectLoader>> annotationTypeProjectLoaderMap = new HashMap<>();
                                for (Row row : result)
                                {
                                    Version currentVersion = new Version(row.getString(8));

                                    ProjectVersion project = PortfolioParser.loadProjectVersion(row.getString(9));     //project_version

                                    project.setCurrentVersion(currentVersion.getVersionUuid());

                                    Map<String, List<String>> uuidDict = ActionOps.getKeyWithArray(row.getString(10));
                                    project.setUuidListDict(uuidDict);                                                      //uuid_project_version

                                    Map<String, List<String>> labelDict = ActionOps.getKeyWithArray(row.getString(11));
                                    project.setLabelListDict(labelDict);                                                    //label_project_version

                                    ProjectLoader loader = ProjectLoader.builder()
                                            .projectId(row.getString(0))                                                   //project_id
                                            .projectName(row.getString(1))                                                 //project_name
                                            .annotationType(row.getInteger(2))                                             //annotation_type
                                            .projectPath(new File(row.getString(3)))                                       //project_path
                                            .projectFilePath(new File(row.getString(4)))                                   //project_file_path
                                            .projectLoaderStatus(ProjectLoaderStatus.DID_NOT_INITIATED)
                                            .isProjectNew(row.getBoolean(5))                                               //is_new
                                            .isProjectStarred(row.getBoolean(6))                                           //is_starred
                                            .projectInfra(ProjectInfra.get(row.getString(7)))                              //project_infra
                                            .projectVersion(project)                                                            //project_version
                                            .build();

                                    projectHandler.loadProjectLoader(loader);
                                    Integer annotationType = loader.getAnnotationType();

                                    if (annotationTypeProjectLoaderMap.containsKey(annotationType))
                                    {
                                        annotationTypeProjectLoaderMap.get(annotationType).add(loader);
                                    }

                                    else
                                    {
                                        List<ProjectLoader> list = new ArrayList<>();
                                        list.add(loader);
                                        annotationTypeProjectLoaderMap.put(annotationType, list);
                                    }
                                }
                                promise.complete(annotationTypeProjectLoaderMap);
                            }
                        },
                        cause -> {
                            log.info("Retrieving from portfolio database to project loader failed");
                            promise.fail(cause);
                        }
                ));
        return promise.future();
    }

    @Override
    public Future<Void> updateUuidVersionList(@NonNull ProjectLoader projectLoader)
    {
        Tuple params = Tuple.of(projectLoader.getProjectVersion().getUuidVersionDbFormat(), projectLoader.getProjectId());
        return queryOps.runQuery(PortfolioDbQuery.getUpdateProject(), params, portfolioPool)
                .map(DBUtils::toVoid);
    }

    @Override
    public Future<Void> startProject(@NonNull String projectID, @NonNull Boolean isStarred)
    {
        Tuple params = Tuple.of(isStarred, projectID);

        return queryOps.runQuery(PortfolioDbQuery.getStarProject(), params, portfolioPool)
                .map(DBUtils::toVoid);
    }

    @Override
    public Future<Void> updateIsNewParam(@NonNull String projectID)
    {
        Promise<Void> promise = Promise.promise();
        ProjectLoader loader = projectHandler.getProjectLoader(projectID);
        Tuple params = Tuple.of(Boolean.FALSE, projectID);

        queryOps.runQuery(PortfolioDbQuery.getUpdateIsNewParam(), params, portfolioPool)
                .onComplete(res -> {
                    if (res.succeeded() && loader != null) {
                        loader.setIsProjectNew(Boolean.FALSE);
                        promise.complete();
                    }

                    else if (res.failed()) {
                        log.info("Update is_new param for project of projectid: " + projectID + " failed");
                        promise.fail(res.cause());
                    }
                });

        return promise.future();
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

//    public ProjectStatisticResponse getProjectStatistic(ProjectLoader projectLoader, AnnotationType type)
//            throws ExecutionException, InterruptedException {
//        int labeledData = 0;
//        int unlabeledData = 0;
//        List<LabelNameAndCountProperties> labelPerClassInProject = new ArrayList<>();
//
//        File projectPath = projectLoader.getProjectPath();
//        if (!projectPath.exists())
//        {
//            log.info(String.format("Root path of project [%s] is missing! %s does not exist.",
//                    projectLoader.getProjectName(), projectLoader.getProjectPath()));
//        }
//
//        if(type == AnnotationType.BOUNDINGBOX || type == AnnotationType.SEGMENTATION) {
//            LabelListHandler labelListHandler = new LabelListHandler();
//            labelListHandler.getImageLabeledStatus(projectLoader.getUuidAnnotationDict());
//            labeledData = labelListHandler.getNumberOfLabeledImage();
//            unlabeledData = labelListHandler.getNumberOfUnLabeledImage();
//            labelPerClassInProject = labelListHandler.getLabelPerClassInProject(projectLoader.getUuidAnnotationDict(), projectLoader);
//        }
//
//        else if(type == AnnotationType.TABULAR) {
//            CompletableFuture<List<JsonObject>> future = new CompletableFuture<>();
//            getAllTabularData(projectLoader.getProjectId()).onComplete(res -> {
//                if(res.succeeded()) {
//                    future.complete(res.result());
//                }
//
//                else if(res.failed()) {
//                    future.completeExceptionally(res.cause());
//                }
//            });
//            List<Map<String, Integer>> list = new ArrayList<>();
//            for(JsonObject result : future.get()){
//                String labelsListString = result.getString("LABEL");
//                if(labelsListString != null) {
//                    labeledData++;
//                    Map<String, Integer> map = new HashMap<>();
//                    JSONArray labelsJsonArray = new JSONArray(labelsListString);
//                    for(int i = 0; i < labelsJsonArray.length(); i++) {
//                        JSONObject jsonObject = labelsJsonArray.getJSONObject(i);
//                        String labelName = jsonObject.getString("labelName");
//                        if(map.containsKey(labelName)) {
//                            map.put(labelName, map.get(labelName) + 1);
//                        } else {
//                            map.put(labelName, 1);
//                        }
//                    }
//                    list.add(map);
//                } else {
//                    unlabeledData++;
//                }
//            }
//            Map<String, Integer> map = list.stream()
//                    .flatMap(m -> m.entrySet().stream())
//                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, Integer::sum));
//
//            labelPerClassInProject = map.entrySet().stream()
//                    .map(m -> LabelNameAndCountProperties.builder().label(m.getKey()).count(m.getValue()).build())
//                    .collect(Collectors.toList());
//        }
//
//        return ProjectStatisticResponse.builder()
//                .message(ReplyHandler.SUCCESSFUL)
//                .numLabeledData(labeledData)
//                .numUnLabeledData(unlabeledData)
//                .labelPerClassInProject(labelPerClassInProject)
//                .build();
//    }

}
