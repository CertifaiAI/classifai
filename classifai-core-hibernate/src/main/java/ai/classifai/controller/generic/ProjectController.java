package ai.classifai.controller.generic;

import ai.classifai.core.entity.dto.generic.VersionDTO;
import ai.classifai.core.entity.dto.generic.LabelDTO;
import ai.classifai.core.entity.dto.generic.ProjectDTO;
import ai.classifai.core.entity.dto.generic.DataDTO;
import ai.classifai.core.entity.model.generic.Label;
import ai.classifai.core.entity.model.generic.Project;
import ai.classifai.core.entity.model.generic.Data;
import ai.classifai.core.entity.trait.HasDTO;
import ai.classifai.database.DbService;
import ai.classifai.selector.status.FileSystemStatus;
import ai.classifai.service.generic.DataService;
import ai.classifai.service.generic.LabelService;
import ai.classifai.service.generic.ProjectLoadingService;
import ai.classifai.service.generic.ProjectService;
import ai.classifai.util.ParamConfig;
import ai.classifai.util.http.HTTPResponseHandler;
import ai.classifai.util.type.AnnotationType;
import ai.classifai.view.ProjectLoadingView;
import ai.classifai.view.ProjectMetaView;
import ai.classifai.view.ProjectReloadView;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class for application logic
 * logic implementation will be done by services
 *
 * @author YinChuangSum
 */
public class ProjectController extends AbstractVertxController
{
    DbService dbService;
    ProjectService projectService;
    LabelService labelService;

    ProjectLoadingService projectLoadingService;

    public ProjectController(Vertx vertx, DbService dbService, ProjectService projectService,
                             LabelService labelService, ProjectLoadingService projectLoadingService)
    {
        super(vertx);
        this.dbService = dbService;
        this.projectService = projectService;
        this.labelService = labelService;
        this.projectLoadingService = projectLoadingService;
    }

    /**
     * Get metadata of all projects by annotation type
     *
     * GET http://localhost:{port}/:annotation_type/projects/meta
     *
     */
    public void getAllProjectsMeta(RoutingContext context)
    {
        AnnotationType annotationType = paramHandler.getAnnotationType(context);

        Future<List<Project>> projectListFuture = dbService.listProjectsByAnnotation(annotationType);

        Future<List<Boolean>> pathValidListFuture = projectListFuture
                .compose(projectService::stateProjectsPathValid);

        Future<List<Boolean>> projectLoadedListFuture = projectListFuture
                .compose(projectLoadingService::stateProjectsLoaded);

        CompositeFuture.all(pathValidListFuture, projectLoadedListFuture)
                .onSuccess(unused ->
                {
                    List<ProjectDTO> projectDTOList = projectListFuture.result()
                            .stream()
                            .map(HasDTO::toDTO)
                            .collect(Collectors.toList());

                    List<VersionDTO> versionDTOList = projectListFuture.result()
                            .stream()
                            .map(Project::getCurrentVersion)
                            .map(HasDTO::toDTO)
                            .collect(Collectors.toList());

                    List<Boolean> pathValidList = pathValidListFuture.result();

                    List<Boolean> projectLoadedList = projectLoadedListFuture.result();

                    JsonObject projectMetaView = new ProjectMetaView().generateMetaList(projectDTOList, versionDTOList,
                            pathValidList, projectLoadedList);

                    sendResponseBody(projectMetaView, context);
                })
                .onFailure(failedRequestHandler(context));
    }

    /**
     * Retrieve specific project metadata
     *
     * GET http://localhost:{port}/:annotation_type/projects/:project_name/meta
     *
     */
    public void getProjectMetadata(RoutingContext context)
    {
        String projectName = paramHandler.getProjectName(context);
        AnnotationType annotationType = paramHandler.getAnnotationType(context);

        Future<Project> projectFuture = dbService.getProjectByNameAndAnnotation(projectName, annotationType);

        Future<Boolean> pathValidFuture = projectFuture
                .compose(projectService::stateProjectPathValid);

        Future<Boolean> projectLoadedFuture = projectFuture
                .compose(projectLoadingService::stateProjectLoaded);

        CompositeFuture.all(pathValidFuture, projectLoadedFuture)
                .onSuccess(unused ->
                {
                    ProjectDTO projectDTO = projectFuture.result().toDTO();
                    VersionDTO versionDTO = projectFuture.result().getCurrentVersion().toDTO();
                    Boolean isPathValid = pathValidFuture.result();
                    Boolean isProjectLoaded = projectLoadedFuture.result();

                    JsonObject projectMetaView = new ProjectMetaView().generateMeta(projectDTO, versionDTO, isPathValid, isProjectLoaded);

                    sendResponseBody(projectMetaView, context);
                })
                .onFailure(failedRequestHandler(context));
    }

    /**
     * Load existing project from the bounding box database
     *
     * GET http://localhost:{port}/:annotation_type/projects/:project_name
     *
     * Example:
     * GET http://localhost:{port}/bndbox/projects/helloworld
     *
     */
    public void loadProject(RoutingContext context)
    {
        // get project, get current version label list, get valid data list
        // add project to loaded list
        String projectName = paramHandler.getProjectName(context);
        AnnotationType annotationType = paramHandler.getAnnotationType(context);

        Future<Project> projectFuture = dbService.getProjectByNameAndAnnotation(projectName, annotationType);

        Future<List<Label>> labelListFuture = projectFuture
                .compose(project -> Future.succeededFuture(project.getCurrentVersion().getLabelList()));

        Future<List<Data>> validDataListFuture = projectFuture
                .compose(project -> Future.succeededFuture(project.getDataList()))
                .compose(dataList -> {
                    DataService dataService = DataService.getDataService(annotationType, vertx);
                    return dataService.filterValidData(dataList);
                });

        CompositeFuture.all(labelListFuture, validDataListFuture)
                .compose(unused -> projectLoadingService.addToLoadedList(projectFuture.result()))
                .compose(unused -> dbService.setProjectIsNew(projectFuture.result(), false))
                .onSuccess(unused ->
                        {
                            List<LabelDTO> labelDTOList = labelListFuture.result()
                                    .stream()
                                    .map(Label::toDTO)
                                    .collect(Collectors.toList());

                            List<DataDTO> dataDTOList = validDataListFuture.result()
                                    .stream()
                                    .map(Data::toDTO)
                                    .collect(Collectors.toList());

                            JsonObject projectLoadingView = new ProjectLoadingView().generateLoadProjectView(labelDTOList, dataDTOList);

                            sendResponseBody(projectLoadingView, context);
                        })
                .onFailure(failedRequestHandler(context));
    }

    /**
     * Get status of loading a project
     *
     * GET http://localhost:{port}/:annotation_type/projects/:project_name/loadingstatus
     *
     * Example:
     * GET http://localhost:{port}/seg/projects/helloworld/loadingstatus
     */
    // FIXME: To be deleted, it can be completed in loadProject(RoutingContext context)
    public void loadProjectStatus(RoutingContext context)
    {
        // get project, get current version label list, get valid data list
        // add project to loaded list
        String projectName = paramHandler.getProjectName(context);
        AnnotationType annotationType = paramHandler.getAnnotationType(context);

        Future<Project> projectFuture = dbService.getProjectByNameAndAnnotation(projectName, annotationType);

        Future<List<Label>> labelListFuture = projectFuture
                .compose(project -> Future.succeededFuture(project.getCurrentVersion().getLabelList()));

        Future<List<Data>> validDataListFuture = projectFuture
                .compose(project -> Future.succeededFuture(project.getDataList()))
                .compose(dataList -> {
                    DataService dataService = DataService.getDataService(annotationType, vertx);
                    return dataService.filterValidData(dataList);
                });

        CompositeFuture.all(labelListFuture, validDataListFuture)
                .compose(unused -> projectLoadingService.addToLoadedList(projectFuture.result()))
                .onSuccess(unused ->
                {
                    List<LabelDTO> labelDTOList = labelListFuture.result()
                            .stream()
                            .map(Label::toDTO)
                            .collect(Collectors.toList());

                    List<DataDTO> dataDTOList = validDataListFuture.result()
                            .stream()
                            .map(Data::toDTO)
                            .collect(Collectors.toList());

                    JsonObject projectLoadingView = new ProjectLoadingView().generateLoadProjectViewStatus(labelDTOList, dataDTOList);

                    sendResponseBody(projectLoadingView, context);
                })
                .onFailure(failedRequestHandler(context));
    }

    /**
     * Delete project
     *
     * DELETE http://localhost:{port}/:annotation_type/projects/:project_name
     *
     * Example:
     * DELETE http://localhost:{port}/bndbox/projects/helloworld
     *
     */
    public void deleteProject(RoutingContext context)
    {
        // get project
        // delete project
        String projectName = paramHandler.getProjectName(context);
        AnnotationType annotationType = paramHandler.getAnnotationType(context);

        dbService.getProjectByNameAndAnnotation(projectName, annotationType)
                .compose(project -> dbService.deleteProject(project))
                .onSuccess(unused -> sendEmptyResponse(context))
                .onFailure(failedRequestHandler(context));
    }

    /***
     * Star a project
     *
     * PUT http://localhost:{port}/:annotation_type/projects/:projectname/star
     */
    public void starProject(RoutingContext context)
    {
        String projectName = paramHandler.getProjectName(context);
        AnnotationType annotationType = paramHandler.getAnnotationType(context);

        context.request().bodyHandler(buffer ->
        {
            // get project
            // star project
            JsonObject body = buffer
                    .toJsonObject();

            Boolean isStarred = Boolean.parseBoolean(body
                    .getString(ParamConfig.getStatusParam()));

            dbService.getProjectByNameAndAnnotation(projectName, annotationType)
                    .compose(project -> dbService.starProject(project, isStarred))
                    .onSuccess(unused -> sendEmptyResponse(context))
                    .onFailure(failedRequestHandler(context));

        });
    }

    /***
     * change is_load state of a project to false
     *
     * PUT http://localhost:{port}/:annotation_type/projects/:project_name
     */
    public void closeProjectState(RoutingContext context)
    {
        // get project
        // remove from loaded list
        String projectName = paramHandler.getProjectName(context);
        AnnotationType annotationType = paramHandler.getAnnotationType(context);

        dbService.getProjectByNameAndAnnotation(projectName, annotationType)
                .compose(project -> projectLoadingService.removeFromLoadedList(project))
                .onSuccess(unused -> sendEmptyResponse(context))
                .onFailure(failedRequestHandler(context));
    }

    // TODO: suggestion => put new project name in body instead of param
    /**
     * Rename project
     * PUT http://localhost:{port}/v2/:annotation_type/:project_name/rename/:new_project_name
     *
     * Example:
     * PUT http://localhost:{port}/v2/bndbox/:project_name/rename/:new_project_name
     *
     */
    public void renameProject(RoutingContext context)
    {
        String projectName = paramHandler.getProjectName(context);
        AnnotationType annotationType = paramHandler.getAnnotationType(context);
        String newProjectName = paramHandler.getNewProjectName(context);

        // check if project name available -> fail future if project exists
        // rename
        dbService.projectNameAndAnnotationAvailable(newProjectName, annotationType)
                .compose(unused -> dbService.getProjectByNameAndAnnotation(projectName, annotationType))
                .compose(project -> dbService.renameProject(project, newProjectName))
                .onSuccess(unused -> sendEmptyResponse(context))
                .onFailure(failedRequestHandler(context));
    }

    /**
     * Reload v2 project
     * PUT http://localhost:{port}/v2/:annotation_type/projects/:project_name/reload
     *
     * Example:
     * PUT http://localhost:{port}/v2/bndbox/projects/helloworld/reload
     *
     */
    // TODO: uncomment after fixing frontend
    public void reloadProject(RoutingContext context)
    {
//        String projectName = paramHandler.getProjectName(context);
//        AnnotationType annotationType = paramHandler.getAnnotationType(context);
//
//        Future<Project> getProjectFuture = dbService.getProjectByNameAndAnnotation(projectName, annotationType);
//
//        Future<List<Data>> getDataListFuture = getProjectFuture
//                .compose(project -> vertx.executeBlocking(promise -> promise.complete(project.getDataList())));
//
//        DataService dataService = DataService.getDataService(annotationType, vertx);
//
//        getDataListFuture
//                .compose(dataList -> dataService.getToAddDataDtoList(dataList, getProjectFuture.result()))
//                .compose(toAddDataList -> dbService.addDataList(toAddDataList, getProjectFuture.result()))
//                .onSuccess(addedDataList ->
//                {
//                    List<DataDTO> addedDataDTOList = addedDataList.stream()
//                            .map(HasDTO::toDTO)
//                            .collect(Collectors.toList());
//
//                    sendResponseBody(ProjectReloadView.generateStatus(addedDataDTOList), context);
//                })
//                .onFailure(failedRequestHandler(context));
        sendEmptyResponse(context); // todo: delete after fixing frontend
    }

    // TODO: to be deleted
    /**
     * Get load status of project
     * GET http://localhost:{port}/v2/:annotation_type/projects/:project_name/reloadstatus
     *
     * Example:
     * GET http://localhost:{port}/v2/bndbox/projects/helloworld/reloadstatus
     *
     */
    public void reloadProjectStatus(RoutingContext context)
    {
        String projectName = paramHandler.getProjectName(context);
        AnnotationType annotationType = paramHandler.getAnnotationType(context);

        Future<Project> getProjectFuture = dbService.getProjectByNameAndAnnotation(projectName, annotationType);

        Future<List<Data>> getDataListFuture = getProjectFuture
                .compose(project -> vertx.executeBlocking(promise -> promise.complete(project.getDataList())));

        DataService dataService = DataService.getDataService(annotationType, vertx);

        getDataListFuture
                .compose(dataList -> dataService.getToAddDataDtoList(dataList, getProjectFuture.result()))
                .compose(toAddDataList -> dbService.addDataList(toAddDataList, getProjectFuture.result()))
                .onSuccess(addedDataList ->
                {
                    List<DataDTO> addedDataDTOList = addedDataList.stream()
                            .map(HasDTO::toDTO)
                            .collect(Collectors.toList());

                    sendResponseBody(new ProjectReloadView().generateStatus(addedDataDTOList), context);
                })
                .onFailure(failedRequestHandler(context));
    }

    /**
     * Create new project
     * PUT http://localhost:{port}/v2/projects
     *
     * Request Body
     * {
     *   "project_name": "test-project",
     *   "annotation_type": "boundingbox",
     *   "project_path": "/Users/codenamwei/Desktop/Education/books",
     *   "label_file_path": "/Users/codenamewei/Downloads/test_label.txt",
     * }
     *
     */
    public void createProject(RoutingContext context)
    {
        context.request().bodyHandler(buffer ->
        {
            JsonObject requestBody = buffer.toJsonObject();

            String projectName = requestBody.getString("project_name");
            String projectPath = requestBody.getString("project_path");
            String labelFilePath = requestBody.getString("label_file_path");
            AnnotationType annotationType = AnnotationType.fromString(requestBody.getString("annotation_type"));

            DataService dataService = DataService.getDataService(annotationType, vertx);

            Future<List<DataDTO>> dataDTOListFuture = dataService.getDataDTOList(projectPath);

            Future<List<LabelDTO>> labelDTOListFuture = labelService.getLabelDtoList(labelFilePath);

            Future<Void> projectAnnotationAndNameAvailableFuture = dbService.projectNameAndAnnotationAvailable(projectName, annotationType);

            VersionDTO versionDTO = VersionDTO.builder().build();

            ProjectDTO projectDTO = ProjectDTO.builder()
                    .name(projectName)
                    .path(projectPath)
                    .type(annotationType.ordinal())
                    .build();

            CompositeFuture.all(dataDTOListFuture, labelDTOListFuture, projectAnnotationAndNameAvailableFuture)
                    .compose(unused ->
                    {
                        List<DataDTO> dataDTOS = dataDTOListFuture.result();
                        List<LabelDTO> labelDTOS = labelDTOListFuture.result();

                        return dbService.createProject(projectDTO, versionDTO, dataDTOS, labelDTOS);
                    })
                    .onSuccess(unused -> sendEmptyResponse(context))
                    .onFailure(failedRequestHandler(context));
        });
    }

    // TODO: to be deleted
    /**
     * Create new project status
     * GET http://localhost:{port}/v2/:annotation_type/projects/:project_name
     *
     * Example:
     * GET http://localhost:{port}/v2/bndbox/projects/helloworld
     */
    public void createProjectStatus(RoutingContext context)
    {
        JsonObject response = compileFileSysStatusResponse(FileSystemStatus.DATABASE_UPDATED);

        response.put(ParamConfig.getUnsupportedImageListParam(), new ArrayList<String>());

        HTTPResponseHandler.configureOK(context, response);
    }
}
