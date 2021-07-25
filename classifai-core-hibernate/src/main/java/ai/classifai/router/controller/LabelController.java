package ai.classifai.router.controller;

import ai.classifai.service.LabelService;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;

public class LabelController extends AbstractVertxController
{
    private LabelService labelService;

    public LabelController(Vertx vertx, LabelService labelService)
    {
        super(vertx);
        this.labelService = labelService;
    }

    /***
     *
     * Update labels
     *
     * PUT http://localhost:{port}/:annotation_type/projects/:project_name/newlabels
     *
     */
    public void updateLabels(RoutingContext context)
    {
//        String projectName = paramHandler.getProjectName(context);
//        AnnotationType annotationType = paramHandler.getAnnotationType(context);
//
//        String newLabelListStr = context.request()
//                .body()
//                .result()
//                .toJsonObject()
//                .getString("label_list");
//
//        List<String> newLabelList = StringHandler.stringToStringList(newLabelListStr);
//
//        Future<List<Label>> getCurrentVersionLabelListFuture = getProjectByNameAndAnnotation(annotationType, projectName)
//                .compose(project -> vertx.executeBlocking(promise ->
//                        promise.complete(project.getProjectCurrentVersion().getLabelList())));
//
//        Future<Void> deleteLabelListFuture = getCurrentVersionLabelListFuture
//                .compose(labelList -> labelService
//                        .getToDeleteLabelListFuture(labelList, newLabelList))
//                .compose(this::deleteLabelListFuture);
//
//        Future<Void> addLabelListFuture = getCurrentVersionLabelListFuture
//                .compose(labelList -> labelService
//                        .getToAddLabelDTOListFuture(labelList, newLabelList))
//                .compose(this::addLabelDTOListFuture);
//
//        CompositeFuture.all(addLabelListFuture, deleteLabelListFuture)
//                .onSuccess(unused -> sendEmptyResponse(context))
//                .onFailure(failedRequestHandler(context));
    }



}
