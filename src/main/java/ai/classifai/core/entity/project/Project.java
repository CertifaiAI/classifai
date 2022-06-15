package ai.classifai.core.entity.project;

import lombok.Builder;

import java.util.List;

@Builder
public class Project implements ProjectEntity {
    String projectName;

    String projectId;

    String projectPath;

    Integer annotationType;

    Integer projectInfra;

    List<String> labelList;

    @Override
    public String getProjectName() {
        return projectName;
    }

    @Override
    public String getProjectId() {
        return projectId;
    }

    @Override
    public Integer getAnnotationType() {
        return annotationType;
    }

    @Override
    public String getProjectPath() {
        return projectPath;
    }

    @Override
    public Integer getProjectInfra() {
        return projectInfra;
    }

    @Override
    public List<String> getLabelList() {
        return labelList;
    }
}
