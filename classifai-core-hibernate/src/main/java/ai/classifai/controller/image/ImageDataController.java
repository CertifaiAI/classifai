package ai.classifai.controller.image;

import ai.classifai.core.entity.dto.generic.*;
import ai.classifai.core.entity.dto.image.ImageDataDTO;
import ai.classifai.core.entity.dto.image.ImageDataVersionDTO;
import ai.classifai.core.entity.model.generic.*;
import ai.classifai.core.entity.model.image.ImageData;
import ai.classifai.core.entity.model.image.annotation.ImageAnnotation;
import ai.classifai.core.entity.trait.HasDTO;
import ai.classifai.database.DbService;
import ai.classifai.controller.generic.AbstractVertxController;
import ai.classifai.service.image.ImageDataService;
import ai.classifai.util.type.AnnotationType;
import ai.classifai.view.data.ImageDataView;
import ai.classifai.view.data.ImageSourceView;
import ai.classifai.view.annotation.AnnotationView;
import ai.classifai.view.dataversion.DataVersionView;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Class to handle ImageData APIs
 *
 * @author YinChuangSum
 */
@Slf4j
public class ImageDataController extends AbstractVertxController
{
    ImageDataService imageDataService;
    DbService dbService;
    
    public ImageDataController(Vertx vertx, DbService dbService, ImageDataService imageDataService)
    {
        super(vertx);
        this.dbService = dbService;
        this.imageDataService = imageDataService;
    }

    /**
     * Retrieve thumbnail with metadata
     *
     * GET http://localhost:{port}/:annotation_type/projects/:project_name/uuid/:uuid/thumbnail
     *
     */
    public void getThumbnail(RoutingContext context)
    {
        UUID dataId = paramHandler.getDataId(context);
        String projectName = paramHandler.getProjectName(context);
        AnnotationType annotationType = paramHandler.getAnnotationType(context);

        Future<Data> getImageDataFuture = dbService.getDataById(dataId);

        Future<String> getThumbnailFuture = getImageDataFuture
                .compose(imageData -> imageDataService.getThumbnail((ImageData) imageData));

        Future<Project> getProjectFuture = dbService.getProjectByNameAndAnnotation(projectName, annotationType);

        Future<Version> getVersionFuture = getProjectFuture
                .compose(project -> Future.succeededFuture(project.getCurrentVersion()));

        Future<DataVersion> getDataVersionFuture = CompositeFuture.all(getImageDataFuture, getVersionFuture)
                .compose(unused ->
                        {
                            UUID imageDataId = getImageDataFuture.result().getId();
                            UUID versionId = getVersionFuture.result().getId();

                            return dbService.getDataVersionById(imageDataId, versionId);
                        });

        Future<List<ImageAnnotation>> getImageAnnotationListFuture = getDataVersionFuture
                .compose(imageDataVersion -> Future.succeededFuture(imageDataVersion.getAnnotations()))
                .compose(annotationList -> Future.succeededFuture(annotationList.stream()
                        .filter(annotation -> annotation instanceof ImageAnnotation)
                        .map(annotation -> (ImageAnnotation) annotation)
                        .collect(Collectors.toList())));

        Future<List<List<Point>>> getPointListsFuture = getImageAnnotationListFuture
                .compose(imageAnnotationList -> Future.succeededFuture(imageAnnotationList.stream()
                        .map(ImageAnnotation::getPointList)
                        .collect(Collectors.toList())));

        Future<List<Label>> getLabelListsFuture = getImageAnnotationListFuture
                .compose(imageAnnotationList -> Future.succeededFuture(imageAnnotationList.stream()
                        .map(Annotation::getLabel)
                        .collect(Collectors.toList())));

        CompositeFuture.all(getThumbnailFuture, getPointListsFuture, getLabelListsFuture)
                .compose(unused -> vertx.<JsonObject>executeBlocking(promise ->
                {
                    try
                    {
                        List<AnnotationDTO> imageAnnotationDTOList = getImageAnnotationListFuture.result().stream()
                                .map(HasDTO::toDTO)
                                .collect(Collectors.toList());

                        List<List<PointDTO>> pointDTOLists = getPointListsFuture.result().stream()
                                .map(pointList -> pointList.stream()
                                        .map(HasDTO::toDTO)
                                        .collect(Collectors.toList()))
                                .collect(Collectors.toList());

                        List<LabelDTO> labelDTOList = getLabelListsFuture.result().stream()
                                .map(HasDTO::toDTO)
                                .collect(Collectors.toList());

                        AnnotationView annotationView = AnnotationView.getAnnotationView(annotationType);

                        promise.complete(annotationView.generateAnnotationView(imageAnnotationDTOList, pointDTOLists, labelDTOList));
                    }
                    catch (Exception e)
                    {
                        String errorMsg = String.format("Unable to generate annotation view: %s", e.getMessage());
                        log.error(errorMsg);
                        promise.fail(errorMsg);
                    }
                }))
                .onSuccess(annotationView ->
                {
                    ProjectDTO projectDTO = getProjectFuture.result().toDTO();
                    ImageDataDTO imageDataDTO = (ImageDataDTO) getImageDataFuture.result().toDTO();
                    ImageDataVersionDTO imageDataVersionDTO = (ImageDataVersionDTO) getDataVersionFuture.result().toDTO();
                    String thumbnail = getThumbnailFuture.result();

                    DataVersionView dataVersionView = DataVersionView.getDataVersionView(annotationType);

                    JsonObject dataVersionViewJson = dataVersionView.generateImageDataVersionView(imageDataVersionDTO);

                    sendResponseBody(new ImageDataView().generateImageDataView(projectDTO, imageDataDTO, thumbnail, dataVersionViewJson, annotationView), context);
                })
                .onFailure(failedRequestHandler(context));
    }

    /***
     *
     * Get Image Source
     *
     * GET http://localhost:{port}/:annotation_type/projects/:project_name/uuid/:uuid/imgsrc
     *
     */
    public void getImageSource(RoutingContext context)
    {
        UUID dataId = paramHandler.getDataId(context);

        dbService.getDataById(dataId)
                .compose(data -> imageDataService.getImageSource((ImageData) data))
                .onSuccess(imageBase64 ->
                        sendResponseBody(ImageSourceView.generateImageSourceView(imageBase64), context))
                .onFailure(failedRequestHandler(context));
    }
}
