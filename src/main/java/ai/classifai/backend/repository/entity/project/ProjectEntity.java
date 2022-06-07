package ai.classifai.backend.repository.entity.project;

import ai.classifai.backend.dto.ProjectDTO;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.List;

@NonNull
@SuperBuilder
@NoArgsConstructor
@Getter
public class ProjectEntity {
    private String projectName;
    private String projectId;
    private int projectType;
    private String projectPath;
    private int projectInfra;
    private List<String> labelList;

    ProjectEntity(ProjectDTO projectDTO) {
        this.projectName = projectDTO.getProjectName();
        this.projectId = projectDTO.getProjectId();
        this.projectPath = projectDTO.getProjectPath();
        this.projectType = projectDTO.getProjectType();
        this.labelList = projectDTO.getLabelList();
        this.projectInfra = projectDTO.getProjectInfra();
    }

    @Override
    public String toString() {
        return "Project{" +
                "projectName='" + projectName + '\'' +
                ", projectId='" + projectId + '\'' +
                ", projectType=" + projectType +
                ", projectPath='" + projectPath + '\'' +
                ", projectInfra=" + projectInfra +
                ", labelList=" + labelList +
                '}';
    }
}
