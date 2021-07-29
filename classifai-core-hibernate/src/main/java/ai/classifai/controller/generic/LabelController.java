package ai.classifai.controller.generic;

import ai.classifai.core.entity.model.generic.Label;
import ai.classifai.core.entity.model.generic.Version;
import ai.classifai.database.DbService;
import ai.classifai.service.generic.LabelService;
import ai.classifai.util.type.AnnotationType;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Class to handle Label APIs
 *
 * @author YinChuangSum
 */
public class LabelController extends AbstractVertxController
{
    private LabelService labelService;
    private DbService dbService;

    public LabelController(Vertx vertx, DbService dbService, LabelService labelService)
    {
        super(vertx);
        this.dbService = dbService;
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
        String projectName = paramHandler.getProjectName(context);
        AnnotationType annotationType = paramHandler.getAnnotationType(context);

        context.request().bodyHandler(buffer ->
        {
            String newLabelListStr = buffer
                    .toJsonObject()
                    .getString("label_list");

            List<String> newLabelList = labelService.getLabelStringListFromString(newLabelListStr);

            Future<Version> getCurrentVersionFuture = dbService.getProjectByNameAndAnnotation(projectName, annotationType)
                    .compose(project -> Future.succeededFuture(project.getCurrentVersion()));

            Future<List<Label>> getCurrentVersionLabelListFuture = getCurrentVersionFuture
                    .compose(version -> Future.succeededFuture(version.getLabelList()));

            Future<Void> deleteLabelListFuture = getCurrentVersionLabelListFuture
                    .compose(labelList ->
                            labelService.getToDeleteLabelListFuture(labelList, newLabelList))
                    .compose(dbService::deleteLabelList);

            Future<Void> addLabelListFuture = getCurrentVersionLabelListFuture
                    .compose(labelList ->
                            labelService.getToAddLabelDTOListFuture(labelList, newLabelList))
                    .compose(labelDTOList -> Future.succeededFuture(labelDTOList.stream()
                            .peek(labelDTO -> labelDTO.setVersionId(getCurrentVersionFuture.result().getId()))
                            .collect(Collectors.toList())))
                    .compose(dbService::addLabelList);

            addLabelListFuture
                    .compose(unused -> deleteLabelListFuture)
                    .onSuccess(unused -> sendEmptyResponse(context))
                    .onFailure(failedRequestHandler(context));
        });
    }


    public void deleteLabel(RoutingContext context)
    {
        UUID label_id = UUID.fromString(context.request().getParam("label_id"));

        dbService.deleteLabelById(label_id)
                .onSuccess(unused -> sendEmptyResponse(context))
                .onFailure(failedRequestHandler(context));
    }
}
