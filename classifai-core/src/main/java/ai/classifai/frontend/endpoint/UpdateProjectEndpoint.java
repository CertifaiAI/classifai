package ai.classifai.frontend.endpoint;

import ai.classifai.core.ProjectOperationService;
import ai.classifai.core.entities.dto.ProjectStatusDTO;
import ai.classifai.core.entities.response.ReloadProjectStatus;
import ai.classifai.core.loader.ProjectLoader;
import ai.classifai.core.util.http.ActionStatus;
import ai.classifai.core.util.http.HTTPResponseHandler;
import ai.classifai.core.util.message.ReplyHandler;
import ai.classifai.core.util.type.AnnotationType;
import ai.classifai.frontend.ui.enums.FileSystemStatus;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Objects;

@Slf4j
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UpdateProjectEndpoint {
    private final ProjectOperationService projectOperationService;

    public UpdateProjectEndpoint(ProjectOperationService projectOperationService) {
        this.projectOperationService = projectOperationService;
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
        AnnotationType type = AnnotationType.getTypeFromEndpoint(annotationType);
        ProjectLoader loader = projectOperationService.getProjectLoader(projectName, type);

        if(loader == null) {
            return HTTPResponseHandler.nullProjectResponse();
        }

        if(projectOperationService.checkValidProjectRename(newProjectName, type.ordinal()))
        {
            return projectOperationService.renameProject(loader, projectName, newProjectName);
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
        AnnotationType type = AnnotationType.getTypeFromEndpoint(annotationType);

        log.info("Reloading project: " + projectName + " of annotation type: " + type.name());

        ProjectLoader loader = projectOperationService.getProjectLoader(projectName, type);

        if(loader == null) {
            return HTTPResponseHandler.nullProjectResponse();
        }

        loader.setFileSystemStatus(FileSystemStatus.ITERATING_FOLDER);

        return projectOperationService.reloadProject(loader);
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
        AnnotationType type = AnnotationType.getTypeFromEndpoint(annotationType);
        ProjectLoader loader = projectOperationService.getProjectLoader(projectName, type);

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

    /***
     * Star a project
     *
     * PUT http://localhost:{port}/:annotation_type/projects/:projectname/star
     */
    @PUT
    @Path("/{annotation_type}/projects/{project_name}/star")
    public Future<ActionStatus> starProject(@PathParam("annotation_type") String annotationType,
                                            @PathParam("project_name") String projectName,
                                            ProjectStatusDTO requestBody)
    {
        AnnotationType type = AnnotationType.getTypeFromEndpoint(annotationType);
        String projectID = projectOperationService.getProjectId(projectName, type.ordinal());

        if(projectID == null) {
            return HTTPResponseHandler.nullProjectResponse();
        }

        Boolean isStarred = Boolean.parseBoolean(requestBody.getStatus());
        return projectOperationService.starProject(projectID, isStarred);
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
                                          ProjectStatusDTO requestBody)
    {
        AnnotationType type = AnnotationType.getTypeFromEndpoint(annotationType);
        String projectID = projectOperationService.getProjectId(projectName, type.ordinal());

        if(projectID == null) {
            return ActionStatus.failDefault();
        }

        if(requestBody.getStatus().equals("closed"))
        {
            Objects.requireNonNull(projectOperationService.getProjectLoader(projectName, type)).setIsLoadedFrontEndToggle(Boolean.FALSE);
        }
        else
        {
            throw new IllegalArgumentException("Request payload failed to satisfied the status of {\"status\": \"closed\"} for " + projectName + ". ");
        }

        return ActionStatus.ok();
    }
}
