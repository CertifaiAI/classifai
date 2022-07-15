package ai.classifai.frontend.api;

import ai.classifai.core.dto.TabularDTO;
import ai.classifai.core.enumeration.AnnotationType;
import ai.classifai.core.loader.ProjectHandler;
import ai.classifai.core.loader.ProjectLoader;
import ai.classifai.core.properties.tabular.TabularProperties;
import ai.classifai.core.service.annotation.TabularAnnotationService;
import ai.classifai.core.utility.handler.ReplyHandler;
import ai.classifai.frontend.request.TabularFileBody;
import ai.classifai.frontend.request.UpdateTabularDataBody;
import ai.classifai.frontend.response.ActionStatus;
import ai.classifai.frontend.response.TabularDataResponse;
import io.vertx.core.Future;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Collections;
import java.util.List;

@Slf4j
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TabularController {

    private final TabularAnnotationService<TabularDTO, TabularProperties> tabularService;
    private final ProjectHandler projectHandler;

    public TabularController(TabularAnnotationService<TabularDTO, TabularProperties> tabularService,
                             ProjectHandler projectHandler) {
        this.tabularService = tabularService;
        this.projectHandler = projectHandler;
    }

    @GET
    @Path("/v2/{annotation_type}/projects/{project_name}/alldata")
    public Future<ActionStatus> getAllTabularData(@PathParam("project_name") String projectName)
    {
        return tabularService.listAnnotations(projectName)
                .compose(tabularService::toJson)
                .map(ActionStatus::okWithResponse)
                .otherwise(res -> ActionStatus.failedWithMessage("Fail to get all annotations for tabular"));
    }

    @GET
    @Path("/v2/{annotation_type}/projects/{project_name}/uuid/{uuid}/data")
    public Future<TabularDataResponse> retrieveSpecificTabularData(@PathParam("project_name") String projectName,
                                                                   @PathParam("uuid") String uuid)
    {
        return tabularService.getAnnotationById(projectName, uuid)
                .compose(res -> tabularService.toJson(Collections.singletonList(res.orElse(null))))
                .map(result -> TabularDataResponse.builder()
                            .message(ReplyHandler.SUCCESSFUL)
                            .tabularData(result.get(0))
                            .build()
                )
                .otherwise(TabularDataResponse.builder()
                        .message(ReplyHandler.FAILED)
                        .errorMessage("Failed to retrieve data")
                        .build());
    }

    @GET
    @Path("/v2/{annotation_type}/projects/{project_name}/invalid")
    public Future<List<String>> getAllInvalidData(@PathParam("project_name") String projectName)
    {
        return tabularService.getAllInvalidData(projectName);
    }

    @PUT
    @Path("/v2/{annotation_type}/projects/{project_name}/updatedata")
    public Future<ActionStatus> updateTabularLabel(@PathParam("annotation_type") String annotationType,
                                                   @PathParam("project_name") String projectName,
                                                   UpdateTabularDataBody requestBody)
    {
        AnnotationType type = AnnotationType.getTypeFromEndPoint(annotationType);
        ProjectLoader loader = projectHandler.getProjectLoader(projectName, type);

        if(loader == null) {
            return Future.succeededFuture(ActionStatus.nullProjectResponse());
        }

        TabularDTO tabularDTO = TabularDTO.builder()
                .uuid(requestBody.getUuid())
                .projectName(projectName)
                .label(requestBody.getLabel())
                .build();

        return tabularService.updateAnnotation(tabularDTO, loader)
                .map(ActionStatus.ok())
                .otherwise(ActionStatus.failedWithMessage("Failure in updating database for " + type + " project: " + projectName));
    }

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
    @POST
    @Path("/v2/{annotation_type}/projects/{project_name}/file")
    public Future<ActionStatus> downloadFile(@PathParam("annotation_type") String annotationType,
                                             @PathParam("project_name") String projectName,
                                             TabularFileBody requestBody) {
        AnnotationType type = AnnotationType.getTypeFromEndPoint(annotationType);
        String projectId = projectHandler.getProjectId(projectName, type.ordinal());
        String fileType = requestBody.getFileType();
        boolean isFilterInvalidData = requestBody.isFilterInvalidData();
        if(projectId == null) {
            return Future.succeededFuture(ActionStatus.nullProjectResponse());
        }

        return tabularService.writeFile(projectId, fileType, isFilterInvalidData)
                .map(ActionStatus.ok())
                .otherwise(ActionStatus.failedWithMessage("Fail to generate " + fileType + " file"));
    }
}
