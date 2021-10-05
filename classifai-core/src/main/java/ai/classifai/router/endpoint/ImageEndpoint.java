package ai.classifai.router.endpoint;

import ai.classifai.database.portfolio.PortfolioDB;
import ai.classifai.dto.api.response.ImageSourceResponse;
import ai.classifai.dto.data.ThumbnailProperties;
import ai.classifai.util.message.ReplyHandler;
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
public class ImageEndpoint {

    private PortfolioDB portfolioDB;

    /**
     * Retrieve thumbnail with metadata
     *
     * GET http://localhost:{port}/:annotation_type/projects/:project_name/uuid/:uuid/thumbnail
     *
     */
    @GET
    @Path("/{annotation_type}/projects/{project_name}/uuid/{uuid}/thumbnail")
    public Future<ThumbnailProperties> getThumbnail(@PathParam("annotation_type") String annotationType,
                                                    @PathParam("project_name") String projectName ,
                                                    @PathParam("uuid") String uuid)
    {
        AnnotationType type = AnnotationHandler.getTypeFromEndpoint(annotationType);
        String projectID = ProjectHandler.getProjectId(projectName, type.ordinal());

        return portfolioDB.getThumbnail(projectID, uuid);
    }

    /***
     *
     * Get Image Source
     *
     * GET http://localhost:{port}/:annotation_type/projects/:project_name/uuid/:uuid/imgsrc
     *
     */
    @GET
    @Path("/{annotation_type}/projects/{project_name}/uuid/{uuid}/imgsrc")
    public Future<ImageSourceResponse> getImageSource(@PathParam("annotation_type") String annotationType,
                                                      @PathParam("project_name") String projectName,
                                                      @PathParam("uuid") String uuid)
    {
        AnnotationType type = AnnotationHandler.getTypeFromEndpoint(annotationType);
        String projectID = ProjectHandler.getProjectId(projectName, type.ordinal());

        return portfolioDB.getImageSource(projectID, uuid, projectName)
                .map(result -> ImageSourceResponse.builder()
                        .message(ReplyHandler.SUCCESSFUL)
                        .imgSrc(result)
                        .build())
                .otherwise(ImageSourceResponse.builder()
                        .message(ReplyHandler.FAILED)
                        .errorMessage("Fail getting image source")
                        .build());
    }
}
