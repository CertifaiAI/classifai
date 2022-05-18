package ai.classifai.router.endpoint;

import ai.classifai.database.portfolio.PortfolioDB;
import ai.classifai.dto.api.response.AudioRegionsResponse;
import ai.classifai.dto.api.response.WaveFormPeaksResponse;
import ai.classifai.dto.data.AudioRegionsProperties;
import ai.classifai.loader.ProjectLoader;
import ai.classifai.util.http.ActionStatus;
import ai.classifai.util.message.ReplyHandler;
import ai.classifai.util.project.ProjectHandler;
import ai.classifai.util.type.AnnotationType;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.vertx.core.Future;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.concurrent.ExecutionException;

@Slf4j
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AudioEndpoint {
    private final PortfolioDB portfolioDB;
    private final ProjectHandler projectHandler;

    public AudioEndpoint(PortfolioDB portfolioDB, ProjectHandler projectHandler) {
        this.portfolioDB = portfolioDB;
        this.projectHandler = projectHandler;
    }

    @POST
    @Path("/v2/{annotation_type}/projects/{project_name}/createregion")
    public Future<ActionStatus> createRegion(@PathParam("annotation_type") String annotationType,
                                             @PathParam("project_name") String projectName,
                                             AudioRegionsProperties audioRegionsProperties) throws JsonProcessingException
    {
        AnnotationType type = AnnotationType.getTypeFromEndpoint(annotationType);
        String projectID = projectHandler.getProjectId(projectName, type.ordinal());
        ProjectLoader projectLoader = projectHandler.getProjectLoader(projectID);

        return portfolioDB.createRegion(projectLoader, audioRegionsProperties)
                .map(ActionStatus.ok())
                .otherwise(ActionStatus.failedWithMessage("Fail to update labels: " + projectName));
    }

    @GET
    @Path("/v2/{annotation_type}/projects/{project_name}/audioregions")
    public Future<AudioRegionsResponse> getAudioRegions(@PathParam("annotation_type") String annotationType,
                                                        @PathParam("project_name") String projectName)
    {
        AnnotationType type = AnnotationType.getTypeFromEndpoint(annotationType);
        String projectID = projectHandler.getProjectId(projectName, type.ordinal());
        ProjectLoader loader = projectHandler.getProjectLoader(projectID);

        return portfolioDB.getAudioRegions(loader)
                .map(result -> AudioRegionsResponse.builder()
                        .message(ReplyHandler.SUCCESSFUL)
                        .listOfRegions(result)
                        .build())
                .otherwise(AudioRegionsResponse.builder()
                        .message(ReplyHandler.FAILED)
                        .errorMessage("Failed to retrieve data")
                        .build());
    }

    @GET
    @Path("/v2/{annotation_type}/projects/{project_name}/audiopeaks")
    public Future<WaveFormPeaksResponse> getWaveFormPeaks(@PathParam("annotation_type") String annotationType,
                                                          @PathParam("project_name") String projectName)
    {
        AnnotationType type = AnnotationType.getTypeFromEndpoint(annotationType);
        String projectID = projectHandler.getProjectId(projectName, type.ordinal());
        ProjectLoader loader = projectHandler.getProjectLoader(projectID);

        return portfolioDB.getWaveFormPeaks(loader)
                .map(result -> WaveFormPeaksResponse.builder()
                        .message(ReplyHandler.SUCCESSFUL)
                        .waveFormPeaks(result)
                        .build())
                .otherwise(WaveFormPeaksResponse.builder()
                        .message(ReplyHandler.FAILED)
                        .errorMessage("Failed to retrieve data")
                        .build());
    }

    @DELETE
    @Path("/v2/{annotation_type}/projects/{project_name}/deleteregion/{uuid}")
    public ActionStatus deleteAudioRegion(@PathParam("annotation_type") String annotationType,
                                          @PathParam("project_name") String projectName,
                                          @PathParam("uuid") String regionId)
    {
        AnnotationType type = AnnotationType.getTypeFromEndpoint(annotationType);
        String projectID = projectHandler.getProjectId(projectName, type.ordinal());
        ProjectLoader loader = projectHandler.getProjectLoader(projectID);

        return portfolioDB.deleteAudioRegion(loader, regionId);
    }

    @PUT
    @Path("/v2/{annotation_type}/projects/{project_name}/updateregion")
    public Future<Void> updateAudioRegion(@PathParam("annotation_type") String annotationType,
                                          @PathParam("project_name") String projectName,
                                          AudioRegionsProperties audioRegionsProperties) throws JsonProcessingException
    {
        AnnotationType type = AnnotationType.getTypeFromEndpoint(annotationType);
        String projectID = projectHandler.getProjectId(projectName, type.ordinal());
        ProjectLoader loader = projectHandler.getProjectLoader(projectID);

        return portfolioDB.updateAudioRegion(loader, audioRegionsProperties);
    }

    @POST
    @Path("/v2/{annotation_type}/projects/{project_name}/saveannotation")
    public Future<ActionStatus> exportAudioAnnotationFile(@PathParam("annotation_type") String annotationType,
                                                          @PathParam("project_name") String projectName) throws ExecutionException, InterruptedException
    {
        AnnotationType type = AnnotationType.getTypeFromEndpoint(annotationType);
        String projectID = projectHandler.getProjectId(projectName, type.ordinal());
        ProjectLoader loader = projectHandler.getProjectLoader(projectID);

        return portfolioDB.exportAudioAnnotation(loader)
                .map(ActionStatus.ok())
                .otherwise(ActionStatus.failedWithMessage("Fail to generate save audio annotation file"));
    }

}
