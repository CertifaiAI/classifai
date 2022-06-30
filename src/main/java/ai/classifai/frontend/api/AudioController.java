package ai.classifai.frontend.api;

import ai.classifai.core.dto.AudioDTO;
import ai.classifai.core.enumeration.AnnotationType;
import ai.classifai.core.loader.ProjectHandler;
import ai.classifai.core.loader.ProjectLoader;
import ai.classifai.core.properties.audio.AudioProperties;
import ai.classifai.core.properties.audio.AudioRegionsProperties;
import ai.classifai.core.service.annotation.AnnotationService;
import ai.classifai.core.utility.handler.ReplyHandler;
import ai.classifai.frontend.response.ActionStatus;
import ai.classifai.frontend.response.AudioRegionsResponse;
import ai.classifai.frontend.response.WaveFormPeaksResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.vertx.core.Future;

import javax.ws.rs.*;

public class AudioController {

    private final AnnotationService<AudioDTO, AudioProperties> audioService;
    private final ProjectHandler projectHandler;

    public AudioController(AnnotationService<AudioDTO, AudioProperties> audioService,
                           ProjectHandler projectHandler) {
        this.audioService = audioService;
        this.projectHandler = projectHandler;
    }

    @POST
    @Path("/v2/{annotation_type}/projects/{project_name}/createregion")
    public Future<ActionStatus> createRegion(@PathParam("annotation_type") String annotationType,
                                             @PathParam("project_name") String projectName,
                                             AudioRegionsProperties audioRegionsProperties) throws Exception
    {
        AudioDTO audioDTO = AudioDTO.builder().build();
        return audioService.createAnnotation(audioDTO)
                .map(ActionStatus.ok())
                .otherwise(ActionStatus.failedWithMessage("Fail to update labels: " + projectName));
    }

//    @GET
//    @Path("/v2/{annotation_type}/projects/{project_name}/audioregions")
//    public Future<AudioRegionsResponse> getAudioRegions(@PathParam("annotation_type") String annotationType,
//                                                        @PathParam("project_name") String projectName)
//    {
//        AnnotationType type = AnnotationType.getTypeFromEndPoint(annotationType);
//        String projectID = projectHandler.getProjectId(projectName, type.ordinal());
//        ProjectLoader loader = projectHandler.getProjectLoader(projectID);
//
//        return audioService.getAnnotationById(loader)
//                .map(result -> AudioRegionsResponse.builder()
//                        .message(ReplyHandler.SUCCESSFUL)
//                        .listOfRegions(result)
//                        .build())
//                .otherwise(AudioRegionsResponse.builder()
//                        .message(ReplyHandler.FAILED)
//                        .errorMessage("Failed to retrieve data")
//                        .build());
//    }
//
//    @GET
//    @Path("/v2/{annotation_type}/projects/{project_name}/audiopeaks")
//    public Future<WaveFormPeaksResponse> getWaveFormPeaks(@PathParam("annotation_type") String annotationType,
//                                                          @PathParam("project_name") String projectName)
//    {
//        AnnotationType type = AnnotationType.getTypeFromEndPoint(annotationType);
//        String projectID = projectHandler.getProjectId(projectName, type.ordinal());
//        ProjectLoader loader = projectHandler.getProjectLoader(projectID);
//
//        return portfolioDB.getWaveFormPeaks(loader)
//                .map(result -> WaveFormPeaksResponse.builder()
//                        .message(ReplyHandler.SUCCESSFUL)
//                        .waveFormPeaks(result)
//                        .build())
//                .otherwise(WaveFormPeaksResponse.builder()
//                        .message(ReplyHandler.FAILED)
//                        .errorMessage("Failed to retrieve data")
//                        .build());
//    }
//
//    @DELETE
//    @Path("/v2/{annotation_type}/projects/{project_name}/deleteregion/{uuid}")
//    public ActionStatus deleteAudioRegion(@PathParam("annotation_type") String annotationType,
//                                          @PathParam("project_name") String projectName,
//                                          @PathParam("uuid") String regionId)
//    {
//        AnnotationType type = AnnotationType.getTypeFromEndPoint(annotationType);
//        String projectID = projectHandler.getProjectId(projectName, type.ordinal());
//        ProjectLoader loader = projectHandler.getProjectLoader(projectID);
//
//        return audioService.deleteData(loader, regionId);
//    }
//
//    @PUT
//    @Path("/v2/{annotation_type}/projects/{project_name}/updateregion")
//    public Future<Void> updateAudioRegion(@PathParam("annotation_type") String annotationType,
//                                          @PathParam("project_name") String projectName,
//                                          AudioRegionsProperties audioRegionsProperties) throws JsonProcessingException
//    {
//        AnnotationType type = AnnotationType.getTypeFromEndPoint(annotationType);
//        String projectID = projectHandler.getProjectId(projectName, type.ordinal());
//        ProjectLoader loader = projectHandler.getProjectLoader(projectID);
//
//        return audioService.updateAnnotation(loader, audioRegionsProperties);
//    }

//    @POST
//    @Path("/v2/{annotation_type}/projects/{project_name}/saveannotation")
//    public Future<ActionStatus> exportAudioAnnotationFile(@PathParam("annotation_type") String annotationType,
//                                                          @PathParam("project_name") String projectName) throws ExecutionException, InterruptedException
//    {
//        AnnotationType type = AnnotationType.getTypeFromEndPoint(annotationType);
//        String projectID = projectHandler.getProjectId(projectName, type.ordinal());
//        ProjectLoader loader = projectHandler.getProjectLoader(projectID);
//
//        return portfolioDB.exportAudioAnnotation(loader)
//                .map(ActionStatus.ok())
//                .otherwise(ActionStatus.failedWithMessage("Fail to generate save audio annotation file"));
//    }
}
