package ai.classifai.database.project;

import ai.classifai.data.AnnotationType;
import ai.classifai.data.ProjectInfra;
import ai.classifai.database.status.FileSystemStatus;
import ai.classifai.database.status.ProjectLoadStatus;

import java.util.List;

public final class VideoProject extends Project {
    private String videoFilePath;
    private Integer videoLength;
    private Integer extractedFrameIndex;
    private String videoDuration;
    private Integer framePerSecond;

    VideoProject(String projectName, String projectId, String projectPath,
                 AnnotationType projectType, List<String> labelList, ProjectInfra projectInfra,
                 FileSystemStatus fileSystemStatus, ProjectLoadStatus projectLoadStatus,
                 String videoFilePath, Integer videoLength, Integer extractedFrameIndex,
                 String videoDuration, Integer framePerSecond)
    {
        super(projectName, projectId, projectPath, projectType, labelList, projectInfra, fileSystemStatus, projectLoadStatus);
        this.videoFilePath = videoFilePath;
        this.videoLength = videoLength;
        this.extractedFrameIndex = extractedFrameIndex;
        this.videoDuration = videoDuration;
        this.framePerSecond = framePerSecond;
    }
}
