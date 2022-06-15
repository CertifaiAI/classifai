package ai.classifai.frontend.api;

import ai.classifai.core.dto.AudioDTO;
import ai.classifai.core.properties.AudioProperties;
import ai.classifai.core.properties.AudioRegionsProperties;
import ai.classifai.core.entity.annotation.AudioEntity;
import ai.classifai.core.service.annotation.AnnotationRepository;
import ai.classifai.core.service.project.ProjectService;
import ai.classifai.frontend.response.ActionStatus;
import io.vertx.core.Future;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

public class AudioController {

    private final AnnotationRepository<AudioEntity, AudioDTO, AudioProperties> audioRepoService;
    private final ProjectService projectService;

    AudioController(AnnotationRepository<AudioEntity, AudioDTO, AudioProperties> audioRepoService,
                    ProjectService projectService) {
        this.audioRepoService = audioRepoService;
        this.projectService = projectService;
    }

    @POST
    @Path("/v2/{annotation_type}/projects/{project_name}/createregion")
    public Future<ActionStatus> createRegion(@PathParam("annotation_type") String annotationType,
                                             @PathParam("project_name") String projectName,
                                             AudioRegionsProperties audioRegionsProperties) throws Exception
    {
        AudioDTO audioDTO = AudioDTO.builder().build();
        return audioRepoService.createAnnotation(audioDTO)
                .map(ActionStatus.ok())
                .otherwise(ActionStatus.failedWithMessage("Fail to update labels: " + projectName));
    }

//    @GET
//    @Path("/v2/{annotation_type}/projects/{project_name}/audioregions")
//    public Future<AudioRegionsResponse> getAudioRegions(@PathParam("annotation_type") String annotationType,
//                                                        @PathParam("project_name") String projectName)
//    {
//        AnnotationType type = AnnotationType.getTypeFromEndpoint(annotationType);
//        String projectID = projectHandler.getProjectId(projectName, type.ordinal());
//        ProjectLoader loader = projectHandler.getProjectLoader(projectID);
//
//        return portfolioDB.getAudioRegions(loader)
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
//        AnnotationType type = AnnotationType.getTypeFromEndpoint(annotationType);
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
//        AnnotationType type = AnnotationType.getTypeFromEndpoint(annotationType);
//        String projectID = projectHandler.getProjectId(projectName, type.ordinal());
//        ProjectLoader loader = projectHandler.getProjectLoader(projectID);
//
//        return portfolioDB.deleteAudioRegion(loader, regionId);
//    }
//
//    @PUT
//    @Path("/v2/{annotation_type}/projects/{project_name}/updateregion")
//    public Future<Void> updateAudioRegion(@PathParam("annotation_type") String annotationType,
//                                          @PathParam("project_name") String projectName,
//                                          AudioRegionsProperties audioRegionsProperties) throws JsonProcessingException
//    {
//        AnnotationType type = AnnotationType.getTypeFromEndpoint(annotationType);
//        String projectID = projectHandler.getProjectId(projectName, type.ordinal());
//        ProjectLoader loader = projectHandler.getProjectLoader(projectID);
//
//        return portfolioDB.updateAudioRegion(loader, audioRegionsProperties);
//    }
//
//    @POST
//    @Path("/v2/{annotation_type}/projects/{project_name}/saveannotation")
//    public Future<ActionStatus> exportAudioAnnotationFile(@PathParam("annotation_type") String annotationType,
//                                                          @PathParam("project_name") String projectName) throws ExecutionException, InterruptedException
//    {
//        AnnotationType type = AnnotationType.getTypeFromEndpoint(annotationType);
//        String projectID = projectHandler.getProjectId(projectName, type.ordinal());
//        ProjectLoader loader = projectHandler.getProjectLoader(projectID);
//
//        return portfolioDB.exportAudioAnnotation(loader)
//                .map(ActionStatus.ok())
//                .otherwise(ActionStatus.failedWithMessage("Fail to generate save audio annotation file"));
//    }
}
