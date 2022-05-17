package ai.classifai.database.project;

import ai.classifai.data.AnnotationType;
import ai.classifai.data.ProjectInfra;

import java.util.List;

public final class TabularProject extends Project {
    private String tabularFilePath;

    TabularProject(String projectName, String projectId, String projectPath,
                   AnnotationType projectType, List<String> labelList, ProjectInfra projectInfra,
                   String tabularFilePath)
    {
        super(projectName, projectId, projectPath, projectType, labelList, projectInfra);
        this.tabularFilePath = tabularFilePath;
    }
}
