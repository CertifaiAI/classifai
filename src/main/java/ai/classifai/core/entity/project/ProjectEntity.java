package ai.classifai.core.entity.project;

import ai.classifai.core.dto.ProjectDTO;
import ai.classifai.core.enumeration.ProjectInfra;

import java.util.List;

public interface ProjectEntity {
    String getProjectName();

    String getProjectId();

    Integer getAnnotationType();

    String getProjectPath();

    ProjectInfra getProjectInfra();

    Boolean getIsRootPathValidParam();

    Boolean getIsProjectNew();

    Boolean getIsProjectStarred();

    Boolean getIsProjectLoaded();

    Boolean getIsCloud();

    String getCreatedDate();

    String getCurrentVersion();

    String getLastModifiedDate();

    String getVersionUuid();

    Integer getExistingDataInDir();

    List<String> getLabelList();

    default ProjectDTO toDto() {
        return ProjectDTO.builder()
                .projectId(getProjectId())
                .projectName(getProjectName())
                .projectPath(getProjectPath())
                .isCloud(getIsCloud())
                .isNewParam(getIsProjectNew())
                .isStarredParam(getIsProjectStarred())
                .isLoadedParam(getIsProjectLoaded())
                .projectInfraParam(getProjectInfra())
                .createdDateParam(getCreatedDate())
                .lastModifiedDate(getLastModifiedDate())
                .currentVersionParam(getVersionUuid())
                .totalUuidParam(getExistingDataInDir())
                .isRootPathValidParam(getIsRootPathValidParam())
                .labelList(getLabelList())
                .currentVersionParam(getCurrentVersion())
                .annotationType(getAnnotationType())
                .build();
    }

}
