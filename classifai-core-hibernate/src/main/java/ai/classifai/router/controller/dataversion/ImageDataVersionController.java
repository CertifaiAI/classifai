package ai.classifai.router.controller.dataversion;

import ai.classifai.core.entity.dto.generic.AnnotationDTO;
import ai.classifai.service.ImageAnnotationService;
import ai.classifai.util.type.AnnotationType;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.List;

public class ImageDataVersionController extends DataVersionController
{
    private ImageAnnotationService imageAnnotationService;

    public ImageDataVersionController(Vertx vertx, ImageAnnotationService imageAnnotationService)
    {
        super(vertx);
        this.imageAnnotationService = imageAnnotationService;
    }

    /***
     *
     * Update labelling information
     *
     * PUT http://localhost:{port}/:annotation_type/projects/:project_name/uuid/:uuid/update
     *
     */
    public void updateData(RoutingContext context)
    {
//        JsonObject requestBody = context.request()
//                .body()
//                .result()
//                .toJsonObject();
//
//        AnnotationType type = paramHandler.getAnnotationType(context);
//
//        List<AnnotationDTO> annotationDTOList = getAnnotationDTOList(requestBody, type);
//
//        DataVersionDTO imageDataVersionDTO = ImageDataVersionDTO.builder().build();
//
//        Future<DataVersion> getImageDataVersionFuture = getDataVersionFuture(context);
//
//        Future<List<Annotation>> getImageAnnotationListFuture = getImageDataVersionFuture
//                .compose(imageDataVersion -> vertx.executeBlocking(promise ->
//                        promise.complete(imageDataVersion.getAnnotations())));
//
//        Future<Void> deleteImageAnnotationListFuture = getImageAnnotationListFuture
//                .compose(imageAnnotations -> imageAnnotationService
//                        .getToDeleteAnnotationListFuture(imageAnnotations, annotationDTOList))
//                .compose(this::deleteAnnotationListFuture);
//
//        Future<Void> addImageAnnotationListFuture = getImageAnnotationListFuture
//                .compose(imageAnnotations -> imageAnnotationService
//                        .getToAddAnnotationListFuture(imageAnnotations, annotationDTOList))
//                .compose(this::addAnnotationDTOListFuture);
//
//        Future<Void> updateImageAnnotationListFuture = getImageAnnotationListFuture
//                .compose(imageAnnotations -> imageAnnotationService
//                        .getToUpdateAnnotationListFuture(imageAnnotations, annotationDTOList))
//                .compose(this::updateAnnotationListFuture);
//
//        Future<Void> updateImageDataVersionFuture = getImageDataVersionFuture
//                .compose(imageDataVersion -> updateDataVersionFuture(imageDataVersion, imageDataVersionDTO));
//
//        CompositeFuture.all(deleteImageAnnotationListFuture, addImageAnnotationListFuture,
//                updateImageAnnotationListFuture, updateImageDataVersionFuture)
//                .onSuccess(unused -> sendEmptyResponse(context))
//                .onFailure(failedRequestHandler(context));
    }

    private List<AnnotationDTO> getAnnotationDTOList(JsonObject requestBody, AnnotationType type)
    {
        JsonArray annotationJsonArray = requestBody.getJsonArray(type.META_KEY);

        return imageAnnotationService.getAnnotationDTOFromJsonArray(annotationJsonArray,
                type);
    }
}
