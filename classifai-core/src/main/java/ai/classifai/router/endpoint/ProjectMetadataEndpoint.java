package ai.classifai.router.endpoint;

import ai.classifai.database.portfolio.PortfolioDB;
import ai.classifai.loader.ProjectLoader;
import ai.classifai.util.http.ActionStatus;
import ai.classifai.util.http.HTTPResponseHandler;
import ai.classifai.util.project.ProjectHandler;
import ai.classifai.util.type.AnnotationHandler;
import ai.classifai.util.type.AnnotationType;
import io.vertx.core.Future;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Slf4j
@Builder
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ProjectMetadataEndpoint {
    private PortfolioDB portfolioDB;

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
        AnnotationType type = AnnotationHandler.getTypeFromEndpoint(annotationType);
        log.info("Get metadata of project: " + projectName + " of annotation type: " + type.name());

        ProjectLoader loader = ProjectHandler.getProjectLoader(projectName, type);
        if(loader == null) {
            return HTTPResponseHandler.nullProjectResponse();
        }

        return portfolioDB.getProjectMetadata(loader.getProjectId())
                .map(ActionStatus::okWithResponse)
                .otherwise(cause -> ActionStatus.failedWithMessage("Failed to retrieve metadata for project " + projectName));
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
        AnnotationType type = AnnotationHandler.getTypeFromEndpoint(annotationType);

        return portfolioDB.getAllProjectsMeta(type.ordinal())
                .map(ActionStatus::okWithResponse)
                .otherwise(cause -> ActionStatus.failedWithMessage("Failure in getting all the projects for " + type.name()));
    }
}
