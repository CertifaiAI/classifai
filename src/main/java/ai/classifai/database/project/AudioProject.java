package ai.classifai.database.project;

import ai.classifai.data.AnnotationType;
import ai.classifai.data.ProjectInfra;
import ai.classifai.database.status.FileSystemStatus;
import ai.classifai.database.status.ProjectLoadStatus;

import java.util.List;

public final class AudioProject extends Project {
    private String audioFilePath;

    AudioProject(String projectName, String projectId, String projectPath,
                 AnnotationType projectType, List<String> labelList, String audioFilePath,
                 ProjectInfra projectInfra, FileSystemStatus fileSystemStatus, ProjectLoadStatus projectLoadStatus)
    {
        super(projectName, projectId, projectPath, projectType, labelList, projectInfra, fileSystemStatus, projectLoadStatus);
        this.audioFilePath = audioFilePath;
    }
}
