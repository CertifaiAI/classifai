package ai.classifai.router.endpoint;

import ai.classifai.action.LabelListImport;
import ai.classifai.database.portfolio.PortfolioDB;
import ai.classifai.dto.api.body.CreateProjectBody;
import ai.classifai.dto.api.body.ProjectStatusBody;
import ai.classifai.dto.api.response.FileSysStatusResponse;
import ai.classifai.dto.api.response.LoadingStatusResponse;
import ai.classifai.dto.api.response.ReloadProjectStatus;
import ai.classifai.dto.api.response.SelectionStatusResponse;
import ai.classifai.loader.ProjectLoader;
import ai.classifai.loader.ProjectLoaderStatus;
import ai.classifai.selector.project.LabelFileSelector;
import ai.classifai.selector.project.ProjectFolderSelector;
import ai.classifai.selector.project.ProjectImportSelector;
import ai.classifai.selector.status.FileSystemStatus;
import ai.classifai.selector.status.NewProjectStatus;
import ai.classifai.selector.status.SelectionWindowStatus;
import ai.classifai.util.collection.UuidGenerator;
import ai.classifai.util.http.ActionStatus;
import ai.classifai.util.http.HTTPResponseHandler;
import ai.classifai.util.message.ReplyHandler;
import ai.classifai.util.project.ProjectHandler;
import ai.classifai.util.project.ProjectInfra;
import ai.classifai.util.type.AnnotationHandler;
import ai.classifai.util.type.AnnotationType;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Classifai v1 endpoints
 *
 * @author devenyantis
 */
@Slf4j
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ProjectEndpoint {

    @Setter private PortfolioDB portfolioDB;
    @Setter private ProjectImportSelector projectImporter = null;
    @Setter private ProjectFolderSelector projectFolderSelector = null;
    @Setter private LabelFileSelector labelFileSelector = null;

    /**
     * Create new project
     * PUT http://localhost:{port}/v2/projects
     *
     * Request Body
     * create raw project
     * {
     *   "status": "raw",
     *   "project_name": "test-project",
     *   "annotation_type": "boundingbox",
     *   "project_path": "/Users/codenamwei/Desktop/Education/books",
     *   "label_file_path": "/Users/codenamewei/Downloads/test_label.txt",
     * }
     *
     * create config project
     * {
     *   "status": "config"
     * }
     */
    @PUT
    @Path("/v2/projects")
    public Future<ActionStatus> createProject(CreateProjectBody requestBody)
    {
        Promise<ActionStatus> promise = Promise.promise();
        try
        {
            String projectStatus = requestBody.getStatus().toUpperCase();

            if(projectStatus.equals(NewProjectStatus.CONFIG.name()))
            {
                projectImporter.run();
                promise.complete(ActionStatus.ok());
            }
            else if(projectStatus.equals(NewProjectStatus.RAW.name()))
            {
                promise.complete(createRawProject(requestBody));
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

    protected ActionStatus createRawProject(CreateProjectBody requestBody) throws IOException {
        String projectName = requestBody.getProjectName();

        String annotationName = requestBody.getAnnotationType();
        Integer annotationInt = AnnotationHandler.getType(annotationName).ordinal();

        if (ProjectHandler.isProjectNameUnique(projectName, annotationInt))
        {
            String projectPath = requestBody.getProjectPath();

            String labelPath = requestBody.getLabelFilePath();
            List<String> labelList = new LabelListImport(new File(labelPath)).getValidLabelList();

            ProjectLoader loader = ProjectLoader.builder()
                    .projectId(UuidGenerator.generateUuid())
                    .projectName(projectName)
                    .annotationType(annotationInt)
                    .projectPath(new File(projectPath))
                    .labelList(labelList)
                    .projectLoaderStatus(ProjectLoaderStatus.LOADED)
                    .projectInfra(ProjectInfra.ON_PREMISE)
                    .fileSystemStatus(FileSystemStatus.ITERATING_FOLDER)
                    .build();

            ProjectHandler.loadProjectLoader(loader);
            loader.initFolderIteration();

            return ActionStatus.ok();
        }

        return ActionStatus.failedWithMessage("Project name exist: " + projectName);
    }

    /**
     * Get import project status
     * GET http://localhost:{port}/v2/:annotation_type/projects/importstatus
     *
     * Example:
     * GET http://localhost:{port}/v2/bndbox/projects/importstatus
     *
     */
    @GET
    @Path("/v2/{annotation_type}/projects/importstatus")
    public FileSysStatusResponse getImportStatus()
    {
        FileSystemStatus fileSysStatus = ProjectImportSelector.getImportFileSystemStatus();

        FileSysStatusResponse response = FileSysStatusResponse.builder()
                .message(ReplyHandler.SUCCESSFUL)
                .fileSystemStatus(fileSysStatus.ordinal())
                .fileSystemMessage(fileSysStatus.name())
                .build();

        if(fileSysStatus.equals(FileSystemStatus.DATABASE_UPDATED))
        {
            response.setProjectName(ProjectImportSelector.getProjectName());
        }

        return response;
    }

    /**
     * Create new project status
     * GET http://localhost:{port}/v2/:annotation_type/projects/:project_name
     *
     * Example:
     * GET http://localhost:{port}/v2/bndbox/projects/helloworld
     */
    @GET
    @Path("/v2/{annotation_type}/projects/{project_name}")
    public FileSysStatusResponse createProjectStatus(@PathParam("annotation_type") String annotationType,
                                                     @PathParam("project_name") String projectName)
    {
        AnnotationType type = AnnotationHandler.getTypeFromEndpoint(annotationType);

        ProjectLoader loader = ProjectHandler.getProjectLoader(projectName, type);

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

    /**
     * Rename project
     * PUT http://localhost:{port}/v2/:annotation_type/projects/:project_name/rename/:new_project_name
     *
     * Example:
     * PUT http://localhost:{port}/v2/bndbox/projects/helloworld/rename/helloworldnewname
     *
     */
    @PUT
    @Path("/v2/{annotation_type}/projects/{project_name}/rename/{new_project_name}")
    public Future<ActionStatus> renameProject(@PathParam("annotation_type") String annotationType,
                                              @PathParam("project_name") String projectName,
                                              @PathParam("new_project_name") String newProjectName)
    {
        AnnotationType type = AnnotationHandler.getTypeFromEndpoint(annotationType);
        ProjectLoader loader = ProjectHandler.getProjectLoader(projectName, type);

        if(loader == null) {
            return HTTPResponseHandler.nullProjectResponse();
        }

        if(ProjectHandler.checkValidProjectRename(newProjectName, type.ordinal()))
        {
            return portfolioDB.renameProject(loader.getProjectId(), newProjectName)
                    .map(result -> {
                        loader.setProjectName(newProjectName);
                        ProjectHandler.updateProjectNameInCache(loader.getProjectId(), loader, projectName);
                        log.debug("Rename to " + newProjectName + " success.");

                        return ActionStatus.ok();
                    })
                    .otherwise(ActionStatus.failedWithMessage("Failed to rename project " + projectName));
        }

        Promise<ActionStatus> promise = Promise.promise();
        promise.complete(ActionStatus.ok());

        return promise.future();
    }

    /**
     * Reload v2 project
     * PUT http://localhost:{port}/v2/:annotation_type/projects/:project_name/reload
     *
     * Example:
     * PUT http://localhost:{port}/v2/bndbox/projects/helloworld/reload
     *
     */
    @PUT
    @Path("/v2/{annotation_type}/projects/{project_name}/reload")
    public Future<ActionStatus> reloadProject(@PathParam("annotation_type") String annotationType,
                                              @PathParam("project_name") String projectName)
    {
        AnnotationType type = AnnotationHandler.getTypeFromEndpoint(annotationType);

        log.info("Reloading project: " + projectName + " of annotation type: " + type.name());

        ProjectLoader loader = ProjectHandler.getProjectLoader(projectName, type);

        if(loader == null) {
            return HTTPResponseHandler.nullProjectResponse();
        }

        loader.setFileSystemStatus(FileSystemStatus.ITERATING_FOLDER);

        return portfolioDB.reloadProject(loader.getProjectId())
                .map(ActionStatus.ok())
                .otherwise(ActionStatus.failedWithMessage("Failed to reload project " + projectName));
    }

    /**
     * Get load status of project
     * GET http://localhost:{port}/v2/:annotation_type/projects/:project_name/reloadstatus
     *
     * Example:
     * GET http://localhost:{port}/v2/bndbox/projects/helloworld/reloadstatus
     *
     */
    @GET
    @Path("/v2/{annotation_type}/projects/{project_name}/reloadstatus")
    public ReloadProjectStatus reloadProjectStatus(@PathParam("annotation_type") String annotationType,
                                                   @PathParam("project_name") String projectName)
    {
        AnnotationType type = AnnotationHandler.getTypeFromEndpoint(annotationType);
        ProjectLoader loader = ProjectHandler.getProjectLoader(projectName, type);

        if(loader == null) {
            return ReloadProjectStatus.builder()
                    .message(ReplyHandler.FAILED)
                    .errorMessage("Project not exist")
                    .build();
        }

        FileSystemStatus fileSysStatus = loader.getFileSystemStatus();

        ReloadProjectStatus response = ReloadProjectStatus.builder()
                .message(ReplyHandler.SUCCESSFUL)
                .fileSystemStatus(fileSysStatus.ordinal())
                .fileSystemMessage(fileSysStatus.name())
                .build();

        if(fileSysStatus.equals(FileSystemStatus.DATABASE_UPDATING))
        {
            response.setProgress(loader.getProgressUpdate());
        }
        else if(fileSysStatus.equals(FileSystemStatus.DATABASE_UPDATED))
        {
            response.setUuidAddList(loader.getReloadAdditionList());
            response.setUuidDeleteList(loader.getReloadDeletionList());
            response.setUnsupportedImageList(loader.getUnsupportedImageList());
        }

        return response;
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
    @GET
    @Path("/{annotation_type}/projects/{project_name}")
    public Future<ActionStatus> loadProject(@PathParam("annotation_type") String annotationType,
                                            @PathParam("project_name") String projectName)
    {
        AnnotationType type = AnnotationHandler.getTypeFromEndpoint(annotationType);

        ProjectLoader loader = ProjectHandler.getProjectLoader(projectName, type);
        Promise<ActionStatus> promise = Promise.promise();

        if(loader == null) {
            return HTTPResponseHandler.nullProjectResponse();
        }

        loader.toggleFrontEndLoaderParam(); //if project is_new = true, change to false since loading the project

        if(loader.isCloud())
        {
            //FIXME
            promise.complete(ActionStatus.ok());
        }
        else
        {
            ProjectLoaderStatus projectLoaderStatus = loader.getProjectLoaderStatus();

            //Project exist, did not load in ProjectLoader, proceed with loading and checking validity of uuid from database
            if(projectLoaderStatus.equals(ProjectLoaderStatus.DID_NOT_INITIATED) || projectLoaderStatus.equals(ProjectLoaderStatus.LOADED))
            {
                loader.setProjectLoaderStatus(ProjectLoaderStatus.LOADING);
                return portfolioDB.loadProject(loader.getProjectId())
                        .map(ActionStatus.ok())
                        .otherwise(ActionStatus.failedWithMessage("Failed to load project " + projectName + ". Check validity of data points failed."));
            }
            else if(projectLoaderStatus.equals(ProjectLoaderStatus.LOADING))
            {
                promise.complete(ActionStatus.failedWithMessage("Loading project is in progress in the backend. Did not reinitiated."));
            }
            else if(projectLoaderStatus.equals(ProjectLoaderStatus.ERROR))
            {
                promise.complete(ActionStatus.failedWithMessage("LoaderStatus with error message when loading project " + projectName + " .Loading project aborted."));
            }
        }

        return promise.future();
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
        AnnotationType type = AnnotationHandler.getTypeFromEndpoint(annotationType);
        ProjectLoader projectLoader = ProjectHandler.getProjectLoader(projectName, type);

        if (projectLoader == null) {
            return LoadingStatusResponse.builder()
                    .message(ProjectLoaderStatus.ERROR.ordinal())
                    .errorMessage("Project not exist")
                    .build();
        }

        ProjectLoaderStatus projectLoaderStatus = projectLoader.getProjectLoaderStatus();

        if (projectLoaderStatus.equals(ProjectLoaderStatus.LOADING)) {
            return LoadingStatusResponse.builder()
                    .message(projectLoaderStatus.ordinal())
                    .progress(projectLoader.getProgress())
                    .build();

        } else if (projectLoaderStatus.equals(ProjectLoaderStatus.LOADED)) {

            // Remove empty string from label list
            projectLoader.getLabelList().removeAll(Collections.singletonList(""));

            // Remove empty string from label list
            projectLoader.getLabelList().removeAll(Collections.singletonList(""));

            return LoadingStatusResponse.builder()
                    .message(projectLoaderStatus.ordinal())
                    .labelList(projectLoader.getLabelList())
                    .sanityUuidList(projectLoader.getSanityUuidList())
                    .build();

        }

        return LoadingStatusResponse.builder()
                .message(ProjectLoaderStatus.ERROR.ordinal())
                .errorMessage("Loading failed. LoaderStatus error for project " + projectName)
                .build();

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
    @DELETE
    @Path("/{annotation_type}/projects/{project_name}")
    public Future<ActionStatus> deleteProject(@PathParam("annotation_type") String annotationType,
                                              @PathParam("project_name") String projectName)
    {
        AnnotationType type = AnnotationHandler.getTypeFromEndpoint(annotationType);
        String projectID = ProjectHandler.getProjectId(projectName, type.ordinal());

        if(projectID == null) {
            return HTTPResponseHandler.nullProjectResponse();
        }

        String errorMessage = "Failure in delete project name: " + projectName + " for " + type.name();

        return portfolioDB.deleteProjectFromPortfolioDb(projectID)
                .compose(result -> portfolioDB.deleteProjectFromAnnotationDb(projectID))
                .map(result -> {
                    ProjectHandler.deleteProjectFromCache(projectID);
                    return ActionStatus.ok();
                })
                .onFailure(cause -> ActionStatus.failedWithMessage(errorMessage));
    }

    /***
     * change is_load state of a project to false
     *
     * PUT http://localhost:{port}/:annotation_type/projects/:project_name
     */
    @PUT
    @Path("/{annotation_type}/projects/{project_name}")
    public ActionStatus closeProjectState(@PathParam("annotation_type") String annotationType,
                                          @PathParam("project_name") String projectName,
                                          ProjectStatusBody requestBody)
    {
        AnnotationType type = AnnotationHandler.getTypeFromEndpoint(annotationType);
        String projectID = ProjectHandler.getProjectId(projectName, type.ordinal());

        if(projectID == null) {
            return ActionStatus.failDefault();
        }

        if(requestBody.getStatus().equals("closed"))
        {
            Objects.requireNonNull(ProjectHandler.getProjectLoader(projectID)).setIsLoadedFrontEndToggle(Boolean.FALSE);
        }
        else
        {
            throw new IllegalArgumentException("Request payload failed to satisfied the status of {\"status\": \"closed\"} for " + projectName + ". ");
        }

        return ActionStatus.ok();
    }

    /***
     * Star a project
     *
     * PUT http://localhost:{port}/:annotation_type/projects/:projectname/star
     */
    @PUT
    @Path("/{annotation_type}/projects/{project_name}/star")
    public Future<ActionStatus> starProject(@PathParam("annotation_type") String annotationType,
                                            @PathParam("project_name") String projectName,
                                            ProjectStatusBody requestBody)
    {
        AnnotationType type = AnnotationHandler.getTypeFromEndpoint(annotationType);
        String projectID = ProjectHandler.getProjectId(projectName, type.ordinal());

        if(projectID == null) {
            return HTTPResponseHandler.nullProjectResponse();
        }

        Boolean isStarred = Boolean.parseBoolean(requestBody.getStatus());
        return portfolioDB.starProject(projectID, isStarred)
                .map(result -> {
                    ProjectLoader loader = Objects.requireNonNull(ProjectHandler.getProjectLoader(projectID));
                    loader.setIsProjectStarred(isStarred);
                    return ActionStatus.ok();
                })
                .otherwise(ActionStatus.failedWithMessage("Star project fail"));
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
        if(!labelFileSelector.isWindowOpen())
        {
            labelFileSelector.run();

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
        SelectionWindowStatus status = labelFileSelector.getWindowStatus();

        SelectionStatusResponse response = SelectionStatusResponse.builder()
                .message(ReplyHandler.SUCCESSFUL)
                .windowStatus(status.ordinal())
                .windowMessage(status.name())
                .build();

        if(status.equals(SelectionWindowStatus.WINDOW_CLOSE))
        {
            response.setLabelFilePath(labelFileSelector.getLabelFilePath());
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
        if(!projectFolderSelector.isWindowOpen())
        {
            projectFolderSelector.run();
        }

        return ActionStatus.ok();
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
        SelectionWindowStatus status = projectFolderSelector.getWindowStatus();

        SelectionStatusResponse response = SelectionStatusResponse.builder()
                .message(ReplyHandler.SUCCESSFUL)
                .windowStatus(status.ordinal())
                .windowMessage(status.name())
                .build();

        if(status.equals(SelectionWindowStatus.WINDOW_CLOSE))
        {
            response.setProjectPath(projectFolderSelector.getProjectFolderPath());
        }

        return response;
    }
}
