package ai.classifai.database.project;

import ai.classifai.data.AnnotationType;
import ai.classifai.data.ProjectInfra;

import java.util.List;

public class VideoProject extends Project{
    private String videoPath;
    private Integer videoLength;
    private Integer extractedFrameIndex;
    private String videoDuration;
    private Integer framePerSecond;

    VideoProject(String projectName, String projectId, String projectPath,
                 AnnotationType projectType, List<String> labelList, ProjectInfra projectInfra,
                 String videoPath, Integer videoLength, Integer extractedFrameIndex,
                 String videoDuration, Integer framePerSecond)
    {
        super(projectName, projectId, projectPath, projectType, labelList, projectInfra);
        this.videoPath = videoPath;
        this.videoLength = videoLength;
        this.extractedFrameIndex = extractedFrameIndex;
        this.videoDuration = videoDuration;
        this.framePerSecond = framePerSecond;
    }
}
