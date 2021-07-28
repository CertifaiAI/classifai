package ai.classifai.controller.image;

import ai.classifai.controller.generic.DataVersionController;
import ai.classifai.core.entity.dto.generic.AnnotationDTO;
import ai.classifai.core.entity.dto.generic.LabelDTO;
import ai.classifai.core.entity.dto.generic.PointDTO;
import ai.classifai.core.entity.model.generic.*;
import ai.classifai.core.entity.model.image.annotation.ImageAnnotation;
import ai.classifai.database.DbService;
import ai.classifai.service.image.ImageAnnotationService;
import ai.classifai.service.generic.PointService;
import ai.classifai.util.type.AnnotationType;
import ai.classifai.view.annotation.AnnotationView;
import ai.classifai.view.dataversion.DataVersionView;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ImageDataVersionController extends DataVersionController
{
    private ImageAnnotationService imageAnnotationService;
    private PointService pointService;
    private DbService dbService;

    public ImageDataVersionController(Vertx vertx, DbService dbService, ImageAnnotationService imageAnnotationService,
                                      PointService pointService)
    {
        super(vertx);
        this.dbService = dbService;
        this.imageAnnotationService = imageAnnotationService;
        this.pointService = pointService;
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
        context.request().bodyHandler(buffer ->
        {
            JsonObject requestBody = buffer.toJsonObject();

            AnnotationType type = paramHandler.getAnnotationType(context);
            String projectName = paramHandler.getProjectName(context);
            UUID dataId = paramHandler.getDataId(context);

            AnnotationView annotationView = AnnotationView.getAnnotationView(type);

            DataVersionView dataVersionView = DataVersionView.getDataVersionView(type);

            Future<Void> decodeAnnotationViewFuture = vertx.executeBlocking(promise ->
            {
                annotationView.decode(requestBody);
                promise.complete();
            });

            Future<Void> decodeDataVersionViewFuture = vertx.executeBlocking(promise ->
            {
                dataVersionView.decode(requestBody);
                promise.complete();
            });

            Future<Version> getVersionFuture = dbService.getProjectByNameAndAnnotation(projectName, type)
                    .compose(project -> Future.succeededFuture(project.getCurrentVersion()));

            Future<DataVersion> getDataVersionFuture = CompositeFuture.all(decodeDataVersionViewFuture, getVersionFuture)
                    .compose(unused -> dbService.getDataVersionById(dataId, getVersionFuture.result().getId()));

            Future<List<Annotation>> getAnnotationListFuture = getDataVersionFuture
                    .compose(dataVersion -> Future.succeededFuture(dataVersion.getAnnotations()));

            Future<List<Label>> getLabelListFuture = getVersionFuture
                    .compose(version -> dbService.listLabelByVersion(version));

            Future<Void> updateDataVersionFuture = getDataVersionFuture
                    .compose(dataVersion -> dbService.updateDataVersion(dataVersion, dataVersionView.getDto()))
                    .mapEmpty();

            // add point here too...
            Future<Void> addAnnotationFuture = CompositeFuture.all(getAnnotationListFuture, decodeAnnotationViewFuture, getLabelListFuture)
                    .compose(unused ->
                    {
                        List<Annotation> annotationList = getAnnotationListFuture.result();
                        List<AnnotationDTO> annotationDTOList = annotationView.getAnnotationDTOList();
                        return imageAnnotationService.getToAddAnnotationFuture(annotationList, annotationDTOList);
                    }).compose(annotationDTO ->
                    {
                        if (annotationDTO == null)
                        {
                            return Future.succeededFuture();
                        }

                        List<List<PointDTO>> pointDTOLists = annotationView.getPointDTOLists();
                        LabelDTO labelDTO = annotationView.getLabelDTOList().get(pointDTOLists.size() - 1);
                        UUID labelId = getLabelListFuture.result().stream()
                                .filter(label -> label.getName().equals(labelDTO.getName()))
                                .map(Label::getId)
                                .findFirst()
                                .get();

                        annotationDTO.setDataId(dataId);
                        annotationDTO.setVersionId(getVersionFuture.result().getId());
                        annotationDTO.setLabelId(labelId);

                        return dbService.addImageAnnotation(annotationDTO, pointDTOLists.get(pointDTOLists.size() - 1)).mapEmpty();
                    });

            // delete cascade...
            Future<Void> deleteAnnotationFuture = CompositeFuture.all(getAnnotationListFuture, decodeAnnotationViewFuture)
                    .compose(unused ->
                    {
                        List<Annotation> annotationList = getAnnotationListFuture.result();
                        List<AnnotationDTO> annotationDTOList = annotationView.getAnnotationDTOList();
                        return imageAnnotationService.getToDeleteAnnotationFuture(annotationList, annotationDTOList);
                    }).compose(annotation ->
                    {
                        if (annotation == null)
                        {
                            return Future.succeededFuture();
                        }

                        return dbService.deleteAnnotation(annotation);
                    });

            // update label, update points
            Future<Void> updateAnnotationLabelFuture = CompositeFuture.all(getAnnotationListFuture, decodeAnnotationViewFuture, getLabelListFuture)
                    .compose(unused ->
                    {
                        List<Annotation> annotationList = getAnnotationListFuture.result();
                        List<LabelDTO> labelDTOList = annotationView.getLabelDTOList();
                        List<Label> labelList = getLabelListFuture.result();

                        return imageAnnotationService.getToUpdateLabelAnnotationFuture(annotationList, labelDTOList, labelList);
                    }).compose(updateAnnotationLabelObject ->
                    {
                        if (updateAnnotationLabelObject == null)
                        {
                            return Future.succeededFuture();
                        }

                        Annotation annotation = updateAnnotationLabelObject.getAnnotation();
                        Label label = updateAnnotationLabelObject.getLabel();

                        return dbService.setAnnotationLabel(annotation, label);
                    }).mapEmpty();


            Future<Void> updatePointFuture = CompositeFuture.all(getAnnotationListFuture, decodeAnnotationViewFuture)
                    .compose(unused ->
                    {
                        List<List<Point>> pointLists = getAnnotationListFuture.result().stream()
                                .map(annotation -> ((ImageAnnotation) annotation).getPointList())
                                .collect(Collectors.toList());
                        List<List<PointDTO>> pointDTOLists = annotationView.getPointDTOLists();

                        return pointService.getToUpdatePointFuture(pointLists, pointDTOLists);
                    }).compose(updatePointObject ->
                    {
                        if (updatePointObject == null)
                        {
                            return Future.succeededFuture();
                        }

                        Point point = updatePointObject.getPoint();
                        PointDTO dto = updatePointObject.getDto();

                        return dbService.updatePoint(point, dto);
                    }).mapEmpty();

            CompositeFuture.all(updateDataVersionFuture, addAnnotationFuture, deleteAnnotationFuture,
                    updateAnnotationLabelFuture, updatePointFuture)
                    .onSuccess(unused -> sendEmptyResponse(context))
                    .onFailure(failedRequestHandler(context));
        });
    }
}
