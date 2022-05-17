package ai.classifai.database.project;

import ai.classifai.data.AnnotationType;
import ai.classifai.data.ProjectInfra;

import java.util.List;

public final class ImageProject extends Project {

    ImageProject(String projectName, String projectId, String projectPath,
                 AnnotationType projectType, List<String> labelList, ProjectInfra projectInfra)
    {
        super(projectName, projectId, projectPath, projectType, labelList, projectInfra);
    }
}
