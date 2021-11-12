package ai.classifai.frontend.endpoint;

import ai.classifai.core.ProjectOperationService;
import ai.classifai.core.loader.ProjectLoader;
import ai.classifai.core.util.http.ActionStatus;
import ai.classifai.core.util.http.HTTPResponseHandler;
import ai.classifai.core.util.type.AnnotationType;
import io.vertx.core.Future;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Slf4j
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ProjectMetadataEndpoint {
    private final ProjectOperationService projectOperationService;

    public ProjectMetadataEndpoint(ProjectOperationService projectOperationService) {
        this.projectOperationService = projectOperationService;
    }

    /**
     * Retrieve specific project metadata
     *
     * GET http://localhost:{port}/:annotation_type/projects/:project_name/meta
     *
     */
    @GET
    @Path("/{annotation_type}/projects/{project_name}/meta")
    public Future<ActionStatus> getProjectMetadata(@PathParam("annotation_type") String annotationType,
                                                   @PathParam("project_name") String projectName)
    {
        AnnotationType type = AnnotationType.getTypeFromEndpoint(annotationType);
        log.info("Get metadata of project: " + projectName + " of annotation type: " + type.name());

        ProjectLoader loader = projectOperationService.getProjectLoader(projectName, type);
        if(loader == null) {
            return HTTPResponseHandler.nullProjectResponse();
        }

        return projectOperationService.getProjectMetadata(loader, projectName);
    }

    /**
     * Get metadata of all projects
     * GET http://localhost:{port}/:annotation_type/projects/meta
     *
     */
    @GET
    @Path("/{annotation_type}/projects/meta")
    public Future<ActionStatus> getAllProjectsMeta(@PathParam("annotation_type") String annotationType)
    {
        AnnotationType type = AnnotationType.getTypeFromEndpoint(annotationType);

        return projectOperationService.getAllProjectsMeta(type);
    }
}
