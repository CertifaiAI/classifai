package ai.classifai.frontend.api;

import ai.classifai.core.data.handler.VideoHandler;
import ai.classifai.core.dto.VideoDTO;
import ai.classifai.core.enumeration.AnnotationType;
import ai.classifai.core.loader.ProjectHandler;
import ai.classifai.core.loader.ProjectLoader;
import ai.classifai.core.properties.video.VideoProperties;
import ai.classifai.core.service.annotation.VideoAnnotationService;
import ai.classifai.frontend.request.VideoFrameExtractBody;
import ai.classifai.frontend.request.VideoFrameRangeExtractBody;
import ai.classifai.frontend.response.ActionStatus;
import ai.classifai.frontend.response.VideoFrameExtractResponse;
import com.zandero.rest.annotation.Get;
import com.zandero.rest.annotation.Post;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Slf4j
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class VideoController {

    private final VideoAnnotationService<VideoDTO, VideoProperties> videoService;
    private final ProjectHandler projectHandler;

    public VideoController(VideoAnnotationService<VideoDTO, VideoProperties> videoService,
                           ProjectHandler projectHandler) {
        this.videoService = videoService;
        this.projectHandler = projectHandler;
    }

    @Post
    @Path("/{annotation_type}/projects/{project_name}/frameextract")
    public Future<ActionStatus> initiateVideoFramesExtraction(@PathParam("annotation_type") String annotationType,
                                                              @PathParam("project_name") String projectName,
                                                              VideoFrameExtractBody videoFrameExtractBody) {

        AnnotationType type = AnnotationType.getTypeFromEndPoint(annotationType);
        ProjectLoader loader = projectHandler.getProjectLoader(projectName, type);
        Promise<ActionStatus> promise = Promise.promise();

        try {
            String videoPath = videoFrameExtractBody.getVideoFilePath();
            Integer extractionPartition = videoFrameExtractBody.getExtractionPartition();
            Integer extractedFrameIndex = videoFrameExtractBody.getExtractionFrameIndex();
            Double currentTime = videoFrameExtractBody.getCurrentTime();

            String projectPath = loader.getProjectPath().toString();
            VideoHandler.extractSpecificFrames(videoPath, projectPath, currentTime);
        }
        catch (Exception e)
        {
            log.info("Video extraction fail");
        }

        promise.complete(ActionStatus.ok());
        return promise.future();
    }

    @Get
    @Path("/{annotation_type}/projects/{project_name}/frameextractstatus")
    public Future<VideoFrameExtractResponse> videoFramesExtractionStatus(@PathParam("annotation_type") String annotationType,
                                                                         @PathParam("project_name") String projectName) {

        int currentTimeStamp = VideoHandler.getTimeStamp();
        int numberOfGeneratedFrame = VideoHandler.getNumOfGeneratedFrames();

        AnnotationType type = AnnotationType.getTypeFromEndPoint(annotationType);
        ProjectLoader loader = projectHandler.getProjectLoader(projectName, type);
        Integer extractedFrameIndex = VideoHandler.getNumOfGeneratedFrames();
        loader.setExtractedFrameIndex(extractedFrameIndex);

        Promise<VideoFrameExtractResponse> promise = Promise.promise();

        VideoFrameExtractResponse response;

        if(VideoHandler.isFinishedExtraction()) {
            loader.setIsVideoExtractionComplete(Boolean.TRUE);
        }

        if(loader.getIsVideoExtractionComplete())
        {
            response = VideoFrameExtractResponse.builder()
                    .message(1)
                    .currentTimeStamp(currentTimeStamp)
                    .isVideoFrameExtractionCompleted(loader.getIsVideoExtractionComplete())
                    .extractedFrameIndex(extractedFrameIndex)
                    .build();
        }
        else
        {
            response = VideoFrameExtractResponse.builder()
                    .message(0)
                    .currentTimeStamp(currentTimeStamp)
                    .isVideoFrameExtractionCompleted(!loader.getIsVideoExtractionComplete())
                    .errorMessage("Fail to extract video frames")
                    .build();
        }

        promise.complete(response);
        return promise.future();
    }

    @Post
    @Path("/{annotation_type}/projects/{project_name}/rangeextract")
    public Future<ActionStatus> initiateTimeRangeExtraction(@PathParam("annotation_type") String annotationType,
                                                            @PathParam("project_name") String projectName,
                                                            VideoFrameRangeExtractBody videoFrameRangeExtractBody) {
        AnnotationType type = AnnotationType.getTypeFromEndPoint(annotationType);
        ProjectLoader loader = projectHandler.getProjectLoader(projectName, type);
        Promise<ActionStatus> promise = Promise.promise();

        try {
            String videoPath = videoFrameRangeExtractBody.getVideoFilePath();
            Double startTime = videoFrameRangeExtractBody.getExtractionStartTime();
            Double endTime = videoFrameRangeExtractBody.getExtractionEndTime();
            Integer partition = videoFrameRangeExtractBody.getExtractionPartition();
            String projectPath = loader.getProjectPath().toString();

            VideoHandler.extractFramesForSelectedTimeRange(videoPath, partition, projectPath, startTime, endTime);
        }
        catch (Exception e)
        {
            log.info("Video extraction fail");
        }

        promise.complete(ActionStatus.ok());
        return promise.future();
    }
}
