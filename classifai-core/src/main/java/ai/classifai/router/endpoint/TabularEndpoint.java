package ai.classifai.router.endpoint;

import ai.classifai.database.portfolio.PortfolioDB;
import ai.classifai.dto.api.body.TabularFileBody;
import ai.classifai.dto.api.body.TabularPreLabellingConditionsBody;
import ai.classifai.dto.api.body.UpdateTabularDataBody;
import ai.classifai.dto.api.response.TabularDataResponse;
import ai.classifai.util.data.TabularHandler;
import ai.classifai.util.http.ActionStatus;
import ai.classifai.util.http.HTTPResponseHandler;
import ai.classifai.util.message.ReplyHandler;
import ai.classifai.util.project.ProjectHandler;
import ai.classifai.util.type.AnnotationType;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Slf4j
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class TabularEndpoint {
    private final PortfolioDB portfolioDB;
    private final ProjectHandler projectHandler;

    public TabularEndpoint(PortfolioDB portfolioDB, ProjectHandler projectHandler) {
        this.portfolioDB = portfolioDB;
        this.projectHandler = projectHandler;
    }

    @GET
    @Path("/v2/{annotation_type}/projects/{project_name}/alldata")
    public Future<List<JsonObject>> getAllTabularData(@PathParam("annotation_type") String annotationType,
                                                      @PathParam("project_name") String projectName)
    {
        AnnotationType type = AnnotationType.getTypeFromEndpoint(annotationType);
        String projectID = projectHandler.getProjectId(projectName, type.ordinal());

        return portfolioDB.getAllTabularData(projectID);
    }

    @GET
    @Path("/v2/{annotation_type}/projects/{project_name}/uuid/{uuid}/data")
    public Future<TabularDataResponse> retrieveSpecificTabularData(@PathParam("annotation_type") String annotationType,
                                                                   @PathParam("project_name") String projectName,
                                                                   @PathParam("uuid") String uuid)
    {
        AnnotationType type = AnnotationType.getTypeFromEndpoint(annotationType);
        String projectID = projectHandler.getProjectId(projectName, type.ordinal());

        return portfolioDB.getTabularDataByUuid(projectID, uuid)
                .map(result -> TabularDataResponse.builder()
                        .message(ReplyHandler.SUCCESSFUL)
                        .tabularData(result)
                        .build())
                .otherwise(TabularDataResponse.builder()
                        .message(ReplyHandler.FAILED)
                        .errorMessage("Failed to retrieve data")
                        .build());
    }

    @GET
    @Path("/v2/{annotation_type}/projects/{project_name}/invalid")
    public Future<List<String>> getAllInvalidData(@PathParam("annotation_type") String annotationType,
                                                      @PathParam("project_name") String projectName)
    {
        AnnotationType type = AnnotationType.getTypeFromEndpoint(annotationType);
        String projectID = projectHandler.getProjectId(projectName, type.ordinal());

        return portfolioDB.getAllInvalidData(projectID);
    }

    @PUT
    @Path("/v2/{annotation_type}/projects/{project_name}/updatedata")
    public Future<ActionStatus> updateTabularLabel(@PathParam("annotation_type") String annotationType,
                                                  @PathParam("project_name") String projectName,
                                                  UpdateTabularDataBody requestBody)
    {
        AnnotationType type = AnnotationType.getTypeFromEndpoint(annotationType);
        String projectID = projectHandler.getProjectId(projectName, type.ordinal());

        if(projectID == null) {
            return HTTPResponseHandler.nullProjectResponse();
        }

        return portfolioDB.updateTabularLabel(projectID, requestBody)
                .map(ActionStatus.ok())
                .otherwise(ActionStatus.failedWithMessage("Failure in updating database for " + type + " project: " + projectName));
    }

    // Note for solve type issue:
    // key value table : every thing in string
    // string int : by casting!
    // store json in database : encode in Hashmap, and identify by casting

    @POST
    @Path("/v2/{annotation_type}/projects/{project_name}/prelabel")
    public ActionStatus preLabellingCondition(@PathParam("annotation_type") String annotationType,
                                              @PathParam("project_name") String projectName,
                                              TabularPreLabellingConditionsBody requestBody)
            throws Exception {
        AnnotationType type = AnnotationType.getTypeFromEndpoint(annotationType);
        String projectId = projectHandler.getProjectId(projectName, type.ordinal());

        TabularHandler tabularHandler = new TabularHandler();
        JsonObject preLabellingConditions = new JsonObject(requestBody.getConditions());
//        String currentUuid = requestBody.getCurrentUuid();
        String labellingMode = requestBody.getLabellingMode();

        tabularHandler.initiateAutomaticLabellingForTabular(projectId, preLabellingConditions, labellingMode, portfolioDB, projectHandler);
        return ActionStatus.ok();

//        return portfolioDB.automateTabularLabelling(projectId, preLabellingConditions, currentUuid, portfolioDB)
//                .map(result -> TabularDataResponse.builder()
//                        .message(ReplyHandler.SUCCESSFUL)
//                        .tabularData(result)
//                        .build())
//                .otherwise(TabularDataResponse.builder()
//                        .message(ReplyHandler.FAILED)
//                        .errorMessage("Failed to retrieve data")
//                        .build());

//        return tabularHandler.returnCurrentUuidFinished(projectId, currentUuid, portfolioDB)
//                .map(result -> TabularDataResponse.builder()
//                        .message(ReplyHandler.SUCCESSFUL)
//                        .tabularData(result)
//                        .build())
//                .otherwise(TabularDataResponse.builder()
//                        .message(ReplyHandler.FAILED)
//                        .errorMessage("Failed to retrieve data")
//                        .build());
    }

    @POST
    @Path("/v2/{annotation_type}/projects/{project_name}/file")
    public Future<ActionStatus> downloadFile(@PathParam("annotation_type") String annotationType,
                                              @PathParam("project_name") String projectName,
                                              TabularFileBody requestBody) throws ExecutionException, InterruptedException, IOException {
        AnnotationType type = AnnotationType.getTypeFromEndpoint(annotationType);
        String projectId = projectHandler.getProjectId(projectName, type.ordinal());
        String fileType = requestBody.getFileType();
        boolean isFilterInvalidData = requestBody.isFilterInvalidData();
        log.info(requestBody.toString());
        if(projectId == null) {
            return HTTPResponseHandler.nullProjectResponse();
        }

        return portfolioDB.writeFile(projectId, fileType, isFilterInvalidData)
                .map(ActionStatus.ok())
                .otherwise(ActionStatus.failedWithMessage("Fail to generate " + fileType + " file"));
    }


}
