package ai.classifai.database.project;

import ai.classifai.data.AnnotationType;
import ai.classifai.data.ProjectInfra;

import java.util.List;

public final class AudioProject extends Project {
    private String audioFilePath;

    AudioProject(String projectName, String projectId, String projectPath,
                 AnnotationType projectType, List<String> labelList, String audioFilePath,
                 ProjectInfra projectInfra)
    {
        super(projectName, projectId, projectPath, projectType, labelList, projectInfra);
        this.audioFilePath = audioFilePath;
    }
}
