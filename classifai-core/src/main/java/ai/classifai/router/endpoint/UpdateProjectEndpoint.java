package ai.classifai.router.endpoint;

import ai.classifai.database.portfolio.PortfolioDB;
import ai.classifai.dto.api.body.ProjectStatusBody;
import ai.classifai.dto.api.response.ReloadProjectStatus;
import ai.classifai.loader.ProjectLoader;
import ai.classifai.ui.enums.FileSystemStatus;
import ai.classifai.util.http.ActionStatus;
import ai.classifai.util.http.HTTPResponseHandler;
import ai.classifai.util.message.ReplyHandler;
import ai.classifai.util.project.ProjectHandler;
import ai.classifai.util.type.AnnotationType;
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
    private final PortfolioDB portfolioDB;
    private final ProjectHandler projectHandler;

    public UpdateProjectEndpoint(PortfolioDB portfolioDB, ProjectHandler projectHandler) {
        this.portfolioDB = portfolioDB;
        this.projectHandler = projectHandler;
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
        ProjectLoader loader = projectHandler.getProjectLoader(projectName, type);

        if(loader == null) {
            return HTTPResponseHandler.nullProjectResponse();
        }

        if(projectHandler.checkValidProjectRename(newProjectName, type.ordinal()))
        {
            return portfolioDB.renameProject(loader.getProjectId(), newProjectName)
                    .map(result -> {
                        loader.setProjectName(newProjectName);
                        projectHandler.updateProjectNameInCache(loader.getProjectId(), loader, projectName);
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
        AnnotationType type = AnnotationType.getTypeFromEndpoint(annotationType);

        log.info("Reloading project: " + projectName + " of annotation type: " + type.name());

        ProjectLoader loader = projectHandler.getProjectLoader(projectName, type);

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
        AnnotationType type = AnnotationType.getTypeFromEndpoint(annotationType);
        ProjectLoader loader = projectHandler.getProjectLoader(projectName, type);

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
                                            ProjectStatusBody requestBody)
    {
        AnnotationType type = AnnotationType.getTypeFromEndpoint(annotationType);
        String projectID = projectHandler.getProjectId(projectName, type.ordinal());

        if(projectID == null) {
            return HTTPResponseHandler.nullProjectResponse();
        }

        Boolean isStarred = Boolean.parseBoolean(requestBody.getStatus());
        return portfolioDB.starProject(projectID, isStarred)
                .map(result -> {
                    ProjectLoader loader = Objects.requireNonNull(projectHandler.getProjectLoader(projectID));
                    loader.setIsProjectStarred(isStarred);
                    return ActionStatus.ok();
                })
                .otherwise(ActionStatus.failedWithMessage("Star project fail"));
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
        AnnotationType type = AnnotationType.getTypeFromEndpoint(annotationType);
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
}
