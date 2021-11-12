package ai.classifai.frontend.endpoint;

import ai.classifai.core.ImageDataService;
import ai.classifai.core.entities.properties.ThumbnailProperties;
import ai.classifai.core.entities.response.ImageSourceResponse;
import ai.classifai.core.util.type.AnnotationType;
import io.vertx.core.Future;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Slf4j
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ImageEndpoint {

    private final ImageDataService imageDataService;

    public ImageEndpoint(ImageDataService imageDataService) {
        this.imageDataService = imageDataService;
    }

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
        AnnotationType type = AnnotationType.getTypeFromEndpoint(annotationType);
        String projectID = imageDataService.getProjectId(projectName, type.ordinal());

        return imageDataService.getThumbnail(projectID, uuid);
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
        AnnotationType type = AnnotationType.getTypeFromEndpoint(annotationType);
        String projectID = imageDataService.getProjectId(projectName, type.ordinal());

        return imageDataService.getImageSource(projectID, uuid, projectName);
    }
}
