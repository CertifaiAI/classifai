package ai.classifai.core.entity.project;

import ai.classifai.core.dto.ProjectDTO;

import java.util.List;

public interface ProjectEntity {
    String getProjectName();

    String getProjectId();

    Integer getAnnotationType();

    String getProjectPath();

    Integer getProjectInfra();

    List<String> getLabelList();

    default ProjectDTO toDto() {
        return ProjectDTO.builder()
                .projectName(getProjectName())
                .projectId(getProjectId())
                .annotationType(getAnnotationType())
                .projectPath(getProjectPath())
                .projectInfra(getProjectInfra())
                .labelList(getLabelList())
                .build();
    }

}
