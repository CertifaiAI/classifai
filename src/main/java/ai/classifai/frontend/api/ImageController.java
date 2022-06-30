package ai.classifai.frontend.api;

import ai.classifai.core.enumeration.AnnotationType;
import ai.classifai.core.loader.ProjectHandler;
import ai.classifai.core.loader.ProjectLoader;
import ai.classifai.core.properties.image.ImageDTO;
import ai.classifai.core.service.annotation.ImageAnnotationService;
import ai.classifai.core.utility.handler.ReplyHandler;
import ai.classifai.frontend.request.ThumbnailProperties;
import ai.classifai.frontend.response.ActionStatus;
import ai.classifai.frontend.response.ImageSourceResponse;
import io.vertx.core.Future;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Slf4j
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ImageController {

    private final ImageAnnotationService<ImageDTO, ThumbnailProperties> imageService;
    private final ProjectHandler projectHandler;

    public ImageController(ImageAnnotationService<ImageDTO, ThumbnailProperties> imageService,
                           ProjectHandler projectHandler) {
        this.imageService = imageService;
        this.projectHandler = projectHandler;
    }

    @GET
    @Path("/{annotation_type}/projects/{project_name}")
    public Future<ActionStatus> listAllAnnotation(@PathParam("project_name") String projectName)
    {
        return imageService.listAnnotations(projectName)
                .map(ActionStatus::okWithResponse)
                .otherwise(res -> ActionStatus.failedWithMessage("Fail to get all annotations for bounding box"));
    }

    @POST
    @Path("/{annotation_type}/projects/{project_name}")
    public Future<ActionStatus> createAnnotation(@PathParam("project_name") String projectName,
                                                 @PathParam("annotation_type") String annotationType,
                                                 ThumbnailProperties imageAnnotationBody) throws Exception
    {
        AnnotationType type = AnnotationType.get(annotationType);
        ProjectLoader loader = projectHandler.getProjectLoader(projectName, type);

        return imageService.createAnnotation(imageService.toDTO(imageAnnotationBody, loader))
                .map(ActionStatus::okWithResponse)
                .otherwise(res -> ActionStatus.failedWithMessage("Fail to create bounding box"));
    }

    @DELETE
    @Path("/{annotation_type}/projects/{project_name}/uuids")
    public Future<ActionStatus> deleteData(@PathParam("project_name") String projectName,
                                           ThumbnailProperties thumbnailProperties)
    {
        return imageService.deleteData(projectName, thumbnailProperties.getUuidParam())
                .map(ActionStatus.ok())
                .otherwise(res -> ActionStatus.failedWithMessage("Fail to delete data"));
    }

    @PUT
    @Path("{annotation_type}/projects/{project_name}/uuid/{uuid}/update")
    public Future<ActionStatus> updateData(@PathParam("project_name") String projectName,
                                           @PathParam("annotation_type") String annotationType,
                                           @PathParam("uuid") String uuid,
                                           ThumbnailProperties thumbnailProperties)
    {
        AnnotationType type = AnnotationType.getTypeFromEndPoint(annotationType);
        ProjectLoader loader = projectHandler.getProjectLoader(projectName, type);

        if (loader == null) {
            return Future.succeededFuture(ActionStatus.nullProjectResponse());
        }

        ImageDTO imageDTO = imageService.toDTO(thumbnailProperties, loader);
        return imageService.updateAnnotation(imageDTO, loader)
                .map(ActionStatus.ok())
                .otherwise(res -> ActionStatus.failedWithMessage("Fail to update data"));
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
                                                    @PathParam("project_name") String projectName,
                                                    @PathParam("uuid") String uuid)
    {
        AnnotationType type = AnnotationType.getTypeFromEndPoint(annotationType);
        ProjectLoader loader = projectHandler.getProjectLoader(projectName, type);

        return imageService.getThumbnail(loader, uuid);
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
        AnnotationType type = AnnotationType.getTypeFromEndPoint(annotationType);
        ProjectLoader loader = projectHandler.getProjectLoader(projectName, type);

        return imageService.getImageSource(loader, uuid)
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
