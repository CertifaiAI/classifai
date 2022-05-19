package ai.classifai.core.services.project;

import ai.classifai.core.entities.ProjectEntity;
import ai.classifai.dto.ProjectDTO;
import ai.classifai.dto.ProjectStatisticsDTO;
import lombok.NonNull;

public interface ProjectQueryService {
    ProjectEntity renameProject(@NonNull ProjectEntity projectEntity, @NonNull ProjectDTO projectDTO);

    ProjectEntity createProject(@NonNull ProjectDTO projectDTO);

    ProjectEntity updateProject(@NonNull ProjectEntity projectEntity, @NonNull ProjectDTO projectDTO);

    void deleteProject(@NonNull ProjectDTO projectDTO);

    ProjectStatisticsDTO getProjectStatistics(@NonNull ProjectEntity projectEntity, @NonNull ProjectDTO projectDTO);

    void updateLabels(@NonNull ProjectEntity projectEntity, @NonNull ProjectDTO projectDTO);
}
