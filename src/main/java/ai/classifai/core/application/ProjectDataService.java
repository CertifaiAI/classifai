package ai.classifai.core.application;

import ai.classifai.core.entities.ProjectEntity;
import ai.classifai.dto.ProjectConfigDTO;
import ai.classifai.dto.ProjectDTO;
import ai.classifai.dto.ProjectStatisticsDTO;
import lombok.NonNull;

import java.util.List;

public interface ProjectDataService {
    List<ProjectEntity> getAllProject();

    ProjectDTO filterProjectById(@NonNull String projectId);

    ProjectEntity createProject(@NonNull ProjectDTO projectDTO);

    ProjectEntity updateProject(@NonNull ProjectEntity projectEntity, @NonNull ProjectDTO projectDTO);

    void deleteProject(@NonNull ProjectDTO projectDTO);

    ProjectEntity renameProject(@NonNull ProjectEntity projectEntity, @NonNull ProjectDTO projectDTO);

    ProjectEntity starProject(@NonNull ProjectEntity projectEntity, @NonNull ProjectDTO projectDTO);

    ProjectEntity loadProject(@NonNull ProjectEntity projectEntity, @NonNull ProjectDTO projectDTO);

    ProjectEntity reloadProject(@NonNull ProjectEntity projectEntity, @NonNull ProjectDTO projectDTO);

    ProjectEntity updateLastModifiedData(@NonNull ProjectEntity projectEntity, @NonNull ProjectDTO projectDTO);

    ProjectEntity importProjectFromConfig(@NonNull ProjectEntity projectEntity, @NonNull ProjectConfigDTO projectConfigFileData);

    ProjectEntity exportProjectConfig(@NonNull ProjectEntity projectEntity, @NonNull ProjectConfigDTO projectConfigFileData);

    ProjectStatisticsDTO getProjectStatistics(@NonNull ProjectEntity projectEntity, @NonNull ProjectDTO projectDTO);

    void updateLabels(@NonNull ProjectEntity projectEntity, @NonNull ProjectDTO projectDTO);

    boolean checkExistProject(@NonNull ProjectEntity projectEntity, @NonNull ProjectDTO projectDTO);

}
