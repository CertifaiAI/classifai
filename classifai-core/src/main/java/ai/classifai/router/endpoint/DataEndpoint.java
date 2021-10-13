package ai.classifai.router.endpoint;

import ai.classifai.action.rename.RenameProjectData;
import ai.classifai.database.portfolio.PortfolioDB;
import ai.classifai.dto.api.body.DeleteProjectDataBody;
import ai.classifai.dto.api.body.RenameDataBody;
import ai.classifai.dto.api.response.DeleteProjectDataResponse;
import ai.classifai.dto.api.response.RenameDataResponse;
import ai.classifai.util.message.ReplyHandler;
import ai.classifai.util.project.ProjectHandler;
import ai.classifai.util.type.AnnotationType;
import io.vertx.core.Future;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class DataEndpoint {

    private final PortfolioDB portfolioDB;
    private final ProjectHandler projectHandler;

    public DataEndpoint(PortfolioDB portfolioDB, ProjectHandler projectHandler) {
        this.portfolioDB = portfolioDB;
        this.projectHandler = projectHandler;
    }

    /**
     * Load data based on uuid
     * DELETE http://localhost:{port}/v2/:annotation_type/projects/:project_name/uuids
     *
     * json payload = {
     *      "uuid_list": ["d99fed36-4eb5-4572-b2c7-ca8d4136e692", "d99fed36-4eb5-4572-b2c7-ca8d4136d2d3f"],
     *      "img_path_list": [
     *              "C:\Users\Deven.Yantis\Desktop\classifai-car-images\12.jpg",
     *              "C:\Users\Deven.Yantis\Desktop\classifai-car-images\1.jpg"
     *       ]
     *  }
     *
     * Example:
     * DELETE http://localhost:{port}/v2/bndbox/projects/helloworld/uuids
     */
    @DELETE
    @Path("/v2/{annotation_type}/projects/{project_name}/uuids")
    public Future<DeleteProjectDataResponse> deleteProjectData(@PathParam("annotation_type") String annotationType,
                                                               @PathParam("project_name") String projectName,
                                                               DeleteProjectDataBody requestBody)
    {
        AnnotationType type = AnnotationType.getTypeFromEndpoint(annotationType);
        String projectID = projectHandler.getProjectId(projectName, type.ordinal());

        if(projectID == null) {
            return Future.succeededFuture(DeleteProjectDataResponse.builder()
                    .message(ReplyHandler.FAILED)
                    .errorMessage("Project not exist")
                    .build());
        }

        return portfolioDB.deleteProjectData(projectID, requestBody.getUuidList(), requestBody.getImgPathList())
                .map(result -> DeleteProjectDataResponse.builder()
                        .message(ReplyHandler.SUCCESSFUL)
                        .uuidList(result)
                        .build())
                .otherwise(DeleteProjectDataResponse.builder()
                        .message(ReplyHandler.FAILED)
                        .errorMessage("Delete project data fail")
                        .build());

    }

    /**
     * Rename data filename
     * PUT http://localhost:{port}/v2/:annotation_type/projects/:project_name/imgsrc/rename
     *
     * Example:
     * PUT http://localhost:{port}/v2/bndbox/projects/helloworld/imgsrc/rename
     *
     * json payload = {
     *      "uuid" : "f592a6e2-53f8-4730-930c-8357d191de48"
     *      "new_fname" : "new_7.jpg"
     * }
     *
     */
    @PUT
    @Path("/v2/{annotation_type}/projects/{project_name}/imgsrc/rename")
    @Produces(MediaType.APPLICATION_JSON)
    public Future<RenameDataResponse> renameData(@PathParam("annotation_type") String annotationType,
                                                 @PathParam("project_name") String projectName,
                                                 RenameDataBody requestBody)
    {
        AnnotationType type = AnnotationType.getTypeFromEndpoint(annotationType);
        String projectId = projectHandler.getProjectId(projectName, type.ordinal());

        return portfolioDB.renameData(projectId, requestBody.getUuid(), requestBody.getNewFilename())
                .map(result -> RenameDataResponse.builder()
                        .message(ReplyHandler.SUCCESSFUL)
                        .imgPath(result)
                        .build())
                .otherwise(cause -> RenameProjectData.reportRenameError(cause.getMessage()));
    }
}
