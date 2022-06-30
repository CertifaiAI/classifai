package ai.classifai.frontend.api;

import ai.classifai.backend.utility.action.LabelListImport;
import ai.classifai.core.dto.ProjectDTO;
import ai.classifai.core.enumeration.AnnotationType;
import ai.classifai.core.loader.ProjectHandler;
import ai.classifai.core.loader.ProjectLoader;
import ai.classifai.core.loader.ProjectLoaderStatus;
import ai.classifai.core.service.NativeUI;
import ai.classifai.core.service.project.ProjectDataService;
import ai.classifai.core.service.project.ProjectService;
import ai.classifai.core.status.FileSystemStatus;
import ai.classifai.core.status.SelectionWindowStatus;
import ai.classifai.core.utility.datetime.DateTime;
import ai.classifai.core.utility.handler.ReplyHandler;
import ai.classifai.core.versioning.Version;
import ai.classifai.frontend.request.CreateProjectBody;
import ai.classifai.frontend.request.LabelListBody;
import ai.classifai.frontend.request.ProjectStatusBody;
import ai.classifai.frontend.response.ActionStatus;
import ai.classifai.frontend.response.FileSysStatusResponse;
import ai.classifai.frontend.response.LoadingStatusResponse;
import ai.classifai.frontend.response.SelectionStatusResponse;
import ai.classifai.frontend.ui.enums.NewProjectStatus;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Slf4j
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ProjectController {
    private final ProjectService projectService;
    private final ProjectDataService projectDataService;
    private final ProjectHandler projectHandler;
    private final NativeUI ui;

    public ProjectController(ProjectService projectService,
                             ProjectDataService projectDataService,
                             ProjectHandler projectHandler,
                             NativeUI ui)
    {
        this.projectService = projectService;
        this.projectDataService = projectDataService;
        this.projectHandler = projectHandler;
        this.ui = ui;
    }

    @POST
    @Path("/v2/projects")
    public Future<ActionStatus> createProject(CreateProjectBody requestBody) {
        Promise<ActionStatus> promise = Promise.promise();
        try
        {
            String projectStatus = requestBody.getStatus().toUpperCase();

            if(projectStatus.equals(NewProjectStatus.CONFIG.name()))
            {
                ui.showProjectImportSelector();
                promise.complete(ActionStatus.ok());
            }
            else if(projectStatus.equals(NewProjectStatus.RAW.name()))
            {
                return createRawProject(requestBody);
            }
            else
            {
                String errorMessage = "Project status: " + projectStatus + " not recognizable. Expect " + NewProjectStatus.getParamList();

                promise.complete(ActionStatus.failedWithMessage(errorMessage));
            }
        }
        catch(Exception e)
        {
            String errorMessage = "Parameter of status with " + NewProjectStatus.getParamList() + " is compulsory in request body";

            promise.complete(ActionStatus.failedWithMessage(errorMessage));
        }

        return promise.future();
    }

    private Future<ActionStatus> createRawProject(CreateProjectBody createProjectBody) {
        Promise<ActionStatus> promise = Promise.promise();

        List<String> labelList = new LabelListImport(new File(createProjectBody.getLabelFilePath())).getValidLabelList();
        ProjectDTO projectDTO = ProjectDTO.builder()
                .projectName(createProjectBody.getProjectName())
                .projectPath(createProjectBody.getProjectPath())
                .annotationType(AnnotationType.get(createProjectBody.getAnnotationType()).ordinal())
                .labelList(labelList)
                .build();

        projectService.createProject(projectDTO)
                .onComplete(res -> {
                    if (res.succeeded()) {
                        ProjectDTO project = res.result();
                        try {
                            projectDataService.parseFileData(project)
                                .compose(projectService::updateUuidVersionList)
                                .onComplete(result -> {
                                    if (result.succeeded()) {
                                        log.debug("Successfully parse files meta data and update database");
                                    }

                                    if (result.failed()) {
                                        log.info("Failed to parse file data. " + result.cause());
                                    }
                                });

                        } catch (Exception e) {
                            log.info("Files are either incompatible type or undefined in folder. " + e);
                        }
                        promise.complete(ActionStatus.okWithResponse(project));
                    }

                    else if (res.failed()) {
                        promise.fail(res.cause());
                    }
                });

        return promise.future();
    }

    @GET
    @Path("/v2/{annotation_type}/projects/{project_name}")
    public FileSysStatusResponse createProjectStatus(@PathParam("annotation_type") String annotationType,
                                                     @PathParam("project_name") String projectName)
    {
        AnnotationType type = AnnotationType.getTypeFromEndPoint(annotationType);
        ProjectLoader loader = projectHandler.getProjectLoader(projectName, type);

        if(loader == null) {
            return FileSysStatusResponse.builder()
                    .message(ReplyHandler.FAILED)
                    .errorMessage("Project not exist: " + projectName)
                    .build();
        }

        FileSystemStatus status = loader.getFileSystemStatus();

        FileSysStatusResponse response = FileSysStatusResponse.builder()
                .message(ReplyHandler.SUCCESSFUL)
                .fileSystemStatus(status.ordinal())
                .fileSystemMessage(status.name())
                .build();

        if (status.equals(FileSystemStatus.DATABASE_UPDATED))
        {
            response.setUnsupportedImageList(loader.getUnsupportedImageList());
        }

        return response;
    }

    @GET
    @Path("/{annotation_type}/projects/meta")
    public Future<ActionStatus> listProjectsMeta(@PathParam("annotation_type") String annotationType)
    {
        return projectService.listProjects(AnnotationType.getTypeFromEndPoint(annotationType).ordinal())
                .map(res -> {
                    if (res.isEmpty()) {
                        return ActionStatus.okWithResponse(null);
                    }
                    return ActionStatus.okWithResponse(res.get());
                })
                .otherwise(cause -> ActionStatus.failedWithMessage("Failed to retrieve projects meta data."));
    }

    @GET
    @Path("/{annotation_type}/projects/{project_name}/meta")
    public Future<ActionStatus> getProjectMetaById(@PathParam("annotation_type") String annotationType,
                                                   @PathParam("project_name") String projectName)
    {
        AnnotationType type = AnnotationType.getTypeFromEndPoint(annotationType);
        ProjectLoader loader = projectHandler.getProjectLoader(projectName, type);

        if (loader == null) {
            return Future.succeededFuture(ActionStatus.nullProjectResponse());
        }

        return projectService.getProjectById(loader)
                .map(res -> ActionStatus.okWithResponse(res.get()))
                .otherwise(cause -> ActionStatus.failedWithMessage("Failed to retrieve project meta data for " + projectName));
    }

    @PUT
    @Path("/{annotation_type}/projects/{project_name}")
    public Future<ActionStatus> updateProject(@PathParam("annotation_type") String annotationType,
                                              @PathParam("project_name") String projectName)
    {
        Integer projectType = AnnotationType.getTypeFromEndPoint(annotationType).ordinal();
        ProjectDTO projectDTO = ProjectDTO.builder().projectName(projectName).annotationType(projectType).build();

        return projectService.updateProject(projectDTO)
                .map(ActionStatus::okWithResponse)
                .otherwise(cause -> ActionStatus.failedWithMessage("Failed to update project meta data for " + projectName));
    }

    @DELETE
    @Path("/{annotation_type}/projects/{project_name}")
    public Future<ActionStatus> deleteProject(@PathParam("annotation_type") String annotationType,
                                              @PathParam("project_name") String projectName)
    {
        Integer projectType = AnnotationType.getTypeFromEndPoint(annotationType).ordinal();
        ProjectDTO projectDTO = ProjectDTO.builder()
                .projectName(projectName)
                .annotationType(projectType)
                .build();

        return projectDataService.deleteProject(projectDTO)
                .compose(res -> projectService.deleteProject(projectDTO))
                .map(res -> ActionStatus.okWithResponse("Delete project: " + projectName))
                .otherwise(cause -> ActionStatus.failedWithMessage("Failed to delete project " + projectName));
    }

    @GET
    @Path("/{annotation_type}/projects/{project_name}/load")
    public Future<ActionStatus> loadProject(@PathParam("annotation_type") String annotationType,
                                            @PathParam("project_name") String projectName)
    {
        AnnotationType type = AnnotationType.getTypeFromEndPoint(annotationType);
        ProjectLoader loader = projectHandler.getProjectLoader(projectName, type);

        if (loader == null)
        {
            return Future.succeededFuture(ActionStatus.nullProjectResponse());
        }

        return projectDataService.loadProject(loader)
                .map(ActionStatus::okWithResponse)
                .otherwise(res -> ActionStatus.failedWithMessage("Failed to load project " + projectName + ". Check validity of data points failed."));
    }


    /**
     * Get status of loading a project
     *
     * GET http://localhost:{port}/:annotation_type/projects/:project_name/loadingstatus
     *
     * Example:
     * GET http://localhost:{port}/seg/projects/helloworld/loadingstatus
     */
    @GET
    @Path("/{annotation_type}/projects/{project_name}/loadingstatus")
    public LoadingStatusResponse loadProjectStatus(@PathParam("annotation_type") String annotationType,
                                                   @PathParam("project_name") String projectName)
    {
        log.info("loading status");
        AnnotationType type = AnnotationType.getTypeFromEndPoint(annotationType);
        ProjectLoader loader = projectHandler.getProjectLoader(projectName, type);

        if (loader == null) {
            return LoadingStatusResponse.builder()
                    .message(ProjectLoaderStatus.ERROR.ordinal())
                    .errorMessage("Project not exist")
                    .build();
        }

        ProjectLoaderStatus projectLoaderStatus = loader.getProjectLoaderStatus();
        log.info("status: " + projectLoaderStatus.name());

        if (projectLoaderStatus.equals(ProjectLoaderStatus.LOADING)) {
            return LoadingStatusResponse.builder()
                    .message(projectLoaderStatus.ordinal())
                    .progress(loader.getProgress())
                    .build();

        } else if (projectLoaderStatus.equals(ProjectLoaderStatus.LOADED)) {
            loader.getLabelList().removeAll(Collections.singletonList(""));

            return LoadingStatusResponse.builder()
                    .message(projectLoaderStatus.ordinal())
                    .labelList(loader.getLabelList())
                    .sanityUuidList(loader.getSanityUuidList())
                    .build();
        }

        return LoadingStatusResponse.builder()
                .message(ProjectLoaderStatus.ERROR.ordinal())
                .errorMessage("Loading failed. LoaderStatus error for project " + projectName)
                .build();

    }

    /**
     * Initiate load label list
     * PUT http://localhost:{port}/v2/labelfiles
     *
     * Example:
     * PUT http://localhost:{port}/v2/labelfiles
     */
    @PUT
    @Path("/v2/labelfiles")
    public ActionStatus selectLabelFile()
    {
        if(!ui.isLabelFileSelectorOpen())
        {
            ui.showLabelFileSelector();
        }

        return ActionStatus.ok();
    }

    /**
     * Get load label file status
     * GET http://localhost:{port}/v2/labelfiles
     *
     * Example:
     * GET http://localhost:{port}/v2/labelfiles
     */
    @GET
    @Path("/v2/labelfiles")
    public SelectionStatusResponse selectLabelFileStatus()
    {
        SelectionWindowStatus status = ui.getLabelFileSelectorWindowStatus();

        SelectionStatusResponse response = SelectionStatusResponse.builder()
                .message(ReplyHandler.SUCCESSFUL)
                .windowStatus(status.ordinal())
                .windowMessage(status.name())
                .build();

        if(status.equals(SelectionWindowStatus.WINDOW_CLOSE))
        {
            response.setLabelFilePath(ui.getLabelFileSelectedPath());
        }

        return response;
    }


    /**
     * Open folder selector to choose project folder
     * PUT http://localhost:{port}/v2/folders
     *
     * Example:
     * PUT http://localhost:{port}/v2/folders
     */
    @PUT
    @Path("/v2/folders")
    public ActionStatus selectProjectFolder()
    {
        if(!ui.isProjectFolderSelectorOpen())
        {
            ui.showProjectFolderSelector();
        }

        return ActionStatus.ok();
    }

    /**
     * Get status of choosing a project folder
     * GET http://localhost:{port}/v2/folders
     *
     * Example:
     * GET http://localhost:{port}/v2/folders
     */
    @GET
    @Path("/v2/folders")
    public SelectionStatusResponse selectProjectFolderStatus()
    {
        SelectionWindowStatus status = ui.getProjectFolderSelectorWindowStatus();

        SelectionStatusResponse response = SelectionStatusResponse.builder()
                .message(ReplyHandler.SUCCESSFUL)
                .windowStatus(status.ordinal())
                .windowMessage(status.name())
                .build();

        if(status.equals(SelectionWindowStatus.WINDOW_CLOSE))
        {
            response.setProjectPath(ui.getProjectFolderSelectedPath());
        }

        return response;
    }

    /**
     * Open folder selector to choose project folder
     * PUT http://localhost:{port}/v2/folders
     *
     * Example:
     * PUT http://localhost:{port}/v2/folders
     */
    @PUT
    @Path("/v2/tabularfile")
    public ActionStatus selectTabularFile()
    {
        if(!ui.isTabularFileSelectorOpen())
        {
            ui.showTabularFileSelector();
        }

        return ActionStatus.ok();
    }

    /**
     * Get status of choosing a project folder
     * GET http://localhost:{port}/v2/folders
     *
     * Example:
     * GET http://localhost:{port}/v2/folders
     */
    @GET
    @Path("/v2/tabularfile")
    public SelectionStatusResponse selectTabularFileStatus()
    {
        SelectionWindowStatus status = ui.getTabularFileSelectorWindowStatus();

        SelectionStatusResponse response = SelectionStatusResponse.builder()
                .message(ReplyHandler.SUCCESSFUL)
                .windowStatus(status.ordinal())
                .windowMessage(status.name())
                .build();

        if(status.equals(SelectionWindowStatus.WINDOW_CLOSE))
        {
            response.setTabularFilePath(ui.getTabularFileSelectedPath());
        }

        return response;
    }

    /**
     * Close/terminate classifai
     * PUT http://localhost:{port}/v2/close
     *
     * Example:
     * PUT http://localhost:{port}/v2/close
     */
    @PUT
    @Path("/v2/close")
    public ActionStatus closeClassifai()
    {
        //terminate after 1 seconds
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        System.exit(0);
                    }
                },
                1000
        );

        return ActionStatus.ok();
    }

    @PUT
    @Path("/{annotation_type}/projects/{project_name}/close")
    public ActionStatus closeProjectState(@PathParam("annotation_type") String annotationType,
                                          @PathParam("project_name") String projectName,
                                          ProjectStatusBody requestBody)
    {
        AnnotationType type = AnnotationType.getTypeFromEndPoint(annotationType);
        String projectID = projectHandler.getProjectId(projectName, type.ordinal());

        if(projectID == null) {
            return ActionStatus.failDefault();
        }

        if(requestBody.getStatus().equals("closed"))
        {
            Objects.requireNonNull(projectHandler.getProjectLoader(projectID)).setIsLoadedFrontEndToggle(Boolean.FALSE);
        }
        else
        {
            throw new IllegalArgumentException("Request payload failed to satisfied the status of {\"status\": \"closed\"} for " + projectName + ". ");
        }

        return ActionStatus.ok();
    }

    @PUT
    @Path("/{annotation_type}/projects/{project_name}/star")
    public Future<ActionStatus> starProject(@PathParam("annotation_type") String annotationType,
                                            @PathParam("project_name") String projectName,
                                            ProjectStatusBody requestBody)
    {
        AnnotationType type = AnnotationType.getTypeFromEndPoint(annotationType);
        String projectID = projectHandler.getProjectId(projectName, type.ordinal());

        if(projectID == null) {
            return Future.succeededFuture(ActionStatus.nullProjectResponse());
        }

        Boolean isStarred = Boolean.parseBoolean(requestBody.getStatus());
        return projectService.starProject(projectID, isStarred)
                .map(result -> {
                    ProjectLoader loader = Objects.requireNonNull(projectHandler.getProjectLoader(projectID));
                    loader.setIsProjectStarred(isStarred);
                    return ActionStatus.ok();
                })
                .otherwise(ActionStatus.failedWithMessage("Star project fail"));
    }


    @PUT
    @Path("/{annotation_type}/projects/{project_name}/newlabels")
    public Future<ActionStatus> updateLabels(@PathParam("annotation_type") String annotationType,
                                             @PathParam("project_name") String projectName,
                                             LabelListBody requestBody)
    {
        AnnotationType type = AnnotationType.getTypeFromEndPoint(annotationType);
        String projectID = projectHandler.getProjectId(projectName, type.ordinal());

        if(projectID == null) {
            return Future.succeededFuture(ActionStatus.nullProjectResponse());
        }

        return projectService.updateLabels(projectID, requestBody.getLabelList())
                .map(ActionStatus.ok())
                .otherwise(ActionStatus.failedWithMessage("Fail to update labels: " + projectName));
    }

}
