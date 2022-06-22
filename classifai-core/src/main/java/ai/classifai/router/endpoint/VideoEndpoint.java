package ai.classifai.router.endpoint;

import ai.classifai.database.portfolio.PortfolioDB;
import ai.classifai.dto.api.body.VideoFrameExtractBody;
import ai.classifai.dto.api.body.VideoFrameRangeExtractBody;
import ai.classifai.dto.api.response.VideoFrameExtractResponse;
import ai.classifai.loader.ProjectLoader;
import ai.classifai.util.data.VideoHandler;
import ai.classifai.util.http.ActionStatus;
import ai.classifai.util.project.ProjectHandler;
import ai.classifai.util.type.AnnotationType;
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
public class VideoEndpoint {
    private final PortfolioDB portfolioDB;
    private final ProjectHandler projectHandler;

    public VideoEndpoint(PortfolioDB portfolioDB, ProjectHandler projectHandler) {
        this.portfolioDB = portfolioDB;
        this.projectHandler = projectHandler;
    }

    @Post
    @Path("/{annotation_type}/projects/{project_name}/frameextract")
    public Future<ActionStatus> initiateVideoFramesExtraction(@PathParam("annotation_type") String annotationType,
                                                              @PathParam("project_name") String projectName,
                                                              VideoFrameExtractBody videoFrameExtractBody) {

        AnnotationType type = AnnotationType.getTypeFromEndpoint(annotationType);
        ProjectLoader loader = projectHandler.getProjectLoader(projectName, type);
        Promise<ActionStatus> promise = Promise.promise();

        try {
            String videoPath = videoFrameExtractBody.getVideoFilePath();
            Integer extractionPartition = videoFrameExtractBody.getExtractionPartition();
            Integer extractedFrameIndex = videoFrameExtractBody.getExtractionFrameIndex();
            Double currentTime = videoFrameExtractBody.getCurrentTime();

            String projectPath = loader.getProjectPath().toString();
            VideoHandler.extractSpecificFrames(videoPath, projectPath, currentTime);
            loader.initVideoFolderIteration();
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

        AnnotationType type = AnnotationType.getTypeFromEndpoint(annotationType);
        ProjectLoader loader = projectHandler.getProjectLoader(projectName, type);
        Integer extractedFrameIndex = VideoHandler.getNumOfGeneratedFrames();
        loader.setExtractedFrameIndex(extractedFrameIndex);

        Promise<VideoFrameExtractResponse> promise = Promise.promise();

        VideoFrameExtractResponse response;

        if(VideoHandler.isFinishedExtraction()) {
            loader.setIsVideoFramesExtractionCompleted(Boolean.TRUE);
        }

        if(loader.getIsVideoFramesExtractionCompleted())
        {
            response = VideoFrameExtractResponse.builder()
                    .message(1)
                    .currentTimeStamp(currentTimeStamp)
                    .isVideoFrameExtractionCompleted(loader.getIsVideoFramesExtractionCompleted())
                    .extractedFrameIndex(extractedFrameIndex)
                    .build();
        }
        else
        {
            response = VideoFrameExtractResponse.builder()
                    .message(0)
                    .currentTimeStamp(currentTimeStamp)
                    .isVideoFrameExtractionCompleted(!loader.getIsVideoFramesExtractionCompleted())
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
        AnnotationType type = AnnotationType.getTypeFromEndpoint(annotationType);
        ProjectLoader loader = projectHandler.getProjectLoader(projectName, type);
        Promise<ActionStatus> promise = Promise.promise();

        try {
            String videoPath = videoFrameRangeExtractBody.getVideoFilePath();
            Double startTime = videoFrameRangeExtractBody.getExtractionStartTime();
            Double endTime = videoFrameRangeExtractBody.getExtractionEndTime();
            Integer partition = videoFrameRangeExtractBody.getExtractionPartition();
            String projectPath = loader.getProjectPath().toString();

            VideoHandler.extractFramesForSelectedTimeRange(videoPath, partition, projectPath, startTime, endTime);
            loader.initVideoFolderIteration();
        }
        catch (Exception e)
        {
            log.info("Video extraction fail");
        }

        promise.complete(ActionStatus.ok());
        return promise.future();
    }
}
