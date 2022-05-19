package ai.classifai.core.application;

import ai.classifai.core.entities.ProjectEntity;
import ai.classifai.core.services.project.ProjectQueryService;
import ai.classifai.dto.ProjectDTO;
import ai.classifai.dto.ProjectStatisticsDTO;
import lombok.NonNull;

public class ProjectService implements ProjectQueryService {
    @Override
    public ProjectEntity renameProject(@NonNull ProjectEntity projectEntity, @NonNull ProjectDTO projectDTO) {
        return null;
    }

    @Override
    public ProjectEntity createProject(@NonNull ProjectDTO projectDTO) {
        return null;
    }

    @Override
    public ProjectEntity updateProject(@NonNull ProjectEntity projectEntity, @NonNull ProjectDTO projectDTO) {
        return null;
    }

    @Override
    public void deleteProject(@NonNull ProjectDTO projectDTO) {

    }

    @Override
    public ProjectStatisticsDTO getProjectStatistics(@NonNull ProjectEntity projectEntity, @NonNull ProjectDTO projectDTO) {
        return null;
    }

    @Override
    public void updateLabels(@NonNull ProjectEntity projectEntity, @NonNull ProjectDTO projectDTO) {

    }
}
