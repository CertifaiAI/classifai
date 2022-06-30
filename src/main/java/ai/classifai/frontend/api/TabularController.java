package ai.classifai.frontend.api;

import ai.classifai.core.data.handler.TabularHandler;
import ai.classifai.core.dto.TabularDTO;
import ai.classifai.core.enumeration.AnnotationType;
import ai.classifai.core.loader.ProjectHandler;
import ai.classifai.core.properties.tabular.TabularProperties;
import ai.classifai.core.service.annotation.AnnotationService;
import ai.classifai.core.utility.handler.ReplyHandler;
import ai.classifai.frontend.request.TabularFileBody;
import ai.classifai.frontend.request.TabularPreLabellingConditionsBody;
import ai.classifai.frontend.request.UpdateTabularDataBody;
import ai.classifai.frontend.response.ActionStatus;
import ai.classifai.frontend.response.TabularDataResponse;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.*;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Slf4j
public class TabularController {

    private final AnnotationService<TabularDTO, TabularProperties> tabularService;
    private final ProjectHandler projectHandler;

    public TabularController(AnnotationService<TabularDTO, TabularProperties> tabularService,
                             ProjectHandler projectHandler) {
        this.tabularService = tabularService;
        this.projectHandler = projectHandler;
    }

//    @GET
//    @Path("/v2/{annotation_type}/projects/{project_name}/alldata")
//    public Future<List<JsonObject>> getAllTabularData(@PathParam("annotation_type") String annotationType,
//                                                      @PathParam("project_name") String projectName)
//    {
//        AnnotationType type = AnnotationType.getTypeFromEndPoint(annotationType);
//        String projectID = projectHandler.getProjectId(projectName, type.ordinal());
//
//        return portfolioDB.getAllTabularData(projectID);
//    }
//
//    @GET
//    @Path("/v2/{annotation_type}/projects/{project_name}/uuid/{uuid}/data")
//    public Future<TabularDataResponse> retrieveSpecificTabularData(@PathParam("annotation_type") String annotationType,
//                                                                   @PathParam("project_name") String projectName,
//                                                                   @PathParam("uuid") String uuid)
//    {
//        AnnotationType type = AnnotationType.getTypeFromEndPoint(annotationType);
//        String projectID = projectHandler.getProjectId(projectName, type.ordinal());
//
//        return portfolioDB.getTabularDataByUuid(projectID, uuid)
//                .map(result -> TabularDataResponse.builder()
//                        .message(ReplyHandler.SUCCESSFUL)
//                        .tabularData(result)
//                        .build())
//                .otherwise(TabularDataResponse.builder()
//                        .message(ReplyHandler.FAILED)
//                        .errorMessage("Failed to retrieve data")
//                        .build());
//    }
//
//    @GET
//    @Path("/v2/{annotation_type}/projects/{project_name}/invalid")
//    public Future<List<String>> getAllInvalidData(@PathParam("annotation_type") String annotationType,
//                                                  @PathParam("project_name") String projectName)
//    {
//        AnnotationType type = AnnotationType.getTypeFromEndPoint(annotationType);
//        String projectID = projectHandler.getProjectId(projectName, type.ordinal());
//
//        return portfolioDB.getAllInvalidData(projectID);
//    }
//
//    @PUT
//    @Path("/v2/{annotation_type}/projects/{project_name}/updatedata")
//    public Future<ActionStatus> updateTabularLabel(@PathParam("annotation_type") String annotationType,
//                                                   @PathParam("project_name") String projectName,
//                                                   UpdateTabularDataBody requestBody)
//    {
//        AnnotationType type = AnnotationType.getTypeFromEndPoint(annotationType);
//        String projectID = projectHandler.getProjectId(projectName, type.ordinal());
//
//        if(projectID == null) {
//            return Future.succeededFuture(ActionStatus.nullProjectResponse());
//        }
//
//        return portfolioDB.updateTabularLabel(projectID, requestBody)
//                .map(ActionStatus.ok())
//                .otherwise(ActionStatus.failedWithMessage("Failure in updating database for " + type + " project: " + projectName));
//    }

    // Note for solve type issue:
    // key value table : every thing in string
    // string int : by casting!
    // store json in database : encode in Hashmap, and identify by casting

//    @POST
//    @Path("/v2/{annotation_type}/projects/{project_name}/prelabel")
//    public ActionStatus preLabellingCondition(@PathParam("annotation_type") String annotationType,
//                                              @PathParam("project_name") String projectName,
//                                              TabularPreLabellingConditionsBody requestBody)
//    public Future<TabularDataResponse> preLabellingCondition(@PathParam("annotation_type") String annotationType,
//                                                             @PathParam("project_name") String projectName,
//                                                             TabularPreLabellingConditionsBody requestBody)
//            throws Exception {
//        AnnotationType type = AnnotationType.getTypeFromEndPoint(annotationType);
//        String projectId = projectHandler.getProjectId(projectName, type.ordinal());
//
//        TabularHandler tabularHandler = new TabularHandler();
//        JsonObject preLabellingConditions = new JsonObject(requestBody.getConditions());
//        String currentUuid = requestBody.getCurrentUuid();
//        String labellingMode = requestBody.getLabellingMode();
//
//        tabularHandler.initiateAutomaticLabellingForTabular(projectId, preLabellingConditions, labellingMode, portfolioDB, projectHandler);
//        return ActionStatus.ok();

//        return portfolioDB.automateTabularLabelling(projectId, preLabellingConditions, currentUuid, labellingMode, portfolioDB)
//                .map(result -> TabularDataResponse.builder()
//                        .message(ReplyHandler.SUCCESSFUL)
//                        .tabularData(result)
//                        .build())
//                .otherwise(TabularDataResponse.builder()
//                        .message(ReplyHandler.FAILED)
//                        .errorMessage("Failed to retrieve data")
//                        .build());
//    }
//
//    @POST
//    @Path("/v2/{annotation_type}/projects/{project_name}/file")
//    public Future<ActionStatus> downloadFile(@PathParam("annotation_type") String annotationType,
//                                             @PathParam("project_name") String projectName,
//                                             TabularFileBody requestBody) throws ExecutionException, InterruptedException, IOException {
//        AnnotationType type = AnnotationType.getTypeFromEndPoint(annotationType);
//        String projectId = projectHandler.getProjectId(projectName, type.ordinal());
//        String fileType = requestBody.getFileType();
//        boolean isFilterInvalidData = requestBody.isFilterInvalidData();
//        log.info(requestBody.toString());
//        if(projectId == null) {
//            return Future.succeededFuture(ActionStatus.nullProjectResponse());
//        }
//
//        return tabularService.writeFile(projectId, fileType, isFilterInvalidData)
//                .map(ActionStatus.ok())
//                .otherwise(ActionStatus.failedWithMessage("Fail to generate " + fileType + " file"));
//    }
}
