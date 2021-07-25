package ai.classifai.router.controller.data;

import ai.classifai.database.DbService;
import ai.classifai.router.controller.AbstractVertxController;
import ai.classifai.service.ImageService;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ImageDataController extends AbstractVertxController
{
    ImageService imageService;
    DbService dbService;
    
    public ImageDataController(Vertx vertx, DbService dbService, ImageService imageService)
    {
        super(vertx);
        this.dbService = dbService;
        this.imageService = imageService;
    }

//    /**
//     * Retrieve thumbnail with metadata
//     *
//     * GET http://localhost:{port}/:annotation_type/projects/:project_name/uuid/:uuid/thumbnail
//     *
//     */
//    // FIXME: rename the function -> getData coz it's getting more than thumbnail
//    //  split data and [dataversion, annotation]
//    public void getThumbnail(RoutingContext context)
//    {
//        UUID dataId = paramHandler.getDataId(context);
//        String projectName = paramHandler.getProjectName(context);
//        AnnotationType annotationType = paramHandler.getAnnotationType(context);
//
//        Future<ImageData> getImageDataFuture = dbService.getImageDataById(dataId);
//
//        Future<String> getThumbnailFuture = getImageDataFuture
//                .compose(imageData -> imageService.getThumbnail(imageData));
//
//        Future<Project> getProjectFuture = dbService.getProjectByNameAndAnnotation(projectName, annotationType);
//
//        Future<Version> getVersionFuture = getProjectFuture
//                .compose(project -> Future.succeededFuture(project.getCurrentVersion()));
//
//        Future<ImageDataVersion> getDataVersionFuture = CompositeFuture.all(getImageDataFuture, getVersionFuture)
//                .compose(unused ->
//                        {
//                            UUID imageDataId = getImageDataFuture.result().getId();
//                            UUID versionId = getVersionFuture.result().getId();
//
//                            return dbService.getImageDataVersionById(imageDataId, versionId);
//                        });
//
//        Future<List<ImageAnnotation>> getImageAnnotationListFuture = getDataVersionFuture
//                .compose(imageDataVersion -> Future.succeededFuture(imageDataVersion.getAnnotations()))
//                .compose(annotationList -> Future.succeededFuture(annotationList.stream()
//                        .filter(annotation -> annotation instanceof ImageAnnotation)
//                        .map(annotation -> (ImageAnnotation) annotation)
//                        .collect(Collectors.toList())));
//
//        Future<List<List<Point>>> getPointListsFuture = getImageAnnotationListFuture
//                .compose(imageAnnotationList -> Future.succeededFuture(imageAnnotationList.stream()
//                        .map(ImageAnnotation::getPointList)
//                        .collect(Collectors.toList())));
//
//        Future<List<Label>> getLabelListsFuture = getImageAnnotationListFuture
//                .compose(imageAnnotationList -> Future.succeededFuture(imageAnnotationList.stream()
//                        .map(Annotation::getLabel)
//                        .collect(Collectors.toList())));
//
//        CompositeFuture.all(getThumbnailFuture, getPointListsFuture, getLabelListsFuture)
//                .compose(unused -> vertx.executeBlocking(promise ->
//                {
//                    try
//                    {
//                        List<AnnotationDTO> imageAnnotationDTOList = getImageAnnotationListFuture.result().stream()
//                                .map(HasDTO::toDTO)
//                                .collect(Collectors.toList());
//
//                        List<List<PointDTO>> pointDTOLists = getPointListsFuture.result().stream()
//                                .map(pointList -> pointList.stream()
//                                        .map(HasDTO::toDTO)
//                                        .collect(Collectors.toList()))
//                                .collect(Collectors.toList());
//
//                        AnnotationView annotationView = AnnotationView.getAnnotationView(getProjectFuture.result().getType());
//
//                        promise.complete(annotationView.generateAnnotationView);
//                    }
//                    catch (Exception e)
//                    {
//                        String errorMsg = String.format("Unable to generate annotation view: %s", e.getMessage());
//                        log.error(errorMsg);
//                        promise.fail(errorMsg);
//                    }
//                }))
//                .onSuccess(unused ->
//                {
//                    ProjectDTO projectDTO = getProjectFuture.result().toDTO();
//                    DataDTO imageDataDTO = getImageDataFuture.result().toDTO();
//                    DataVersionDTO imageDataVersionDTO = getDataVersionFuture.result().toDTO();
//                    List<AnnotationDTO> imageAnnotationDTOList = getAnnotationListFuture.result().stream()
//                            .map(Annotation::toDTO)
//                            .collect(Collectors.toList());
//                    String thumbnail = getThumbnailFuture.result();
//
//                    sendResponseBody(ImageDataView.generateImageDataView(projectDTO, imageDataDTO, imageDataVersionDTO, imageAnnotationDTOList, thumbnail), context);
//                })
//                .onFailure(failedRequestHandler(context));
//    }

    /***
     *
     * Get Image Source
     *
     * GET http://localhost:{port}/:annotation_type/projects/:project_name/uuid/:uuid/imgsrc
     *
     */
    public void getImageSource(RoutingContext context)
    {
//        UUID dataId = UUID.fromString(paramHandler.getDataId(context));
//
//        getData(dataId)
//                .compose(data -> imageService.getImageSource(data))
//                .onSuccess(imageBase64 ->
//                        sendResponseBody(ImageSourceView.generateImageSourceView(imageBase64), context))
//                .onFailure(failedRequestHandler(context));
    }
}
