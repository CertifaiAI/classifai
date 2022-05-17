package ai.classifai.core.services;

import ai.classifai.core.application.ProjectDataService;
import ai.classifai.core.entities.ProjectEntity;
import ai.classifai.dto.ProjectConfigDTO;
import ai.classifai.dto.ProjectDTO;
import ai.classifai.dto.ProjectStatisticsDTO;
import lombok.NonNull;

import java.util.List;

public class ProjectService implements ProjectDataService {
    @Override
    public List<ProjectEntity> getAllProject() {
        return null;
    }

    @Override
    public ProjectDTO filterProjectById(@NonNull String projectId) {
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
    public ProjectEntity renameProject(@NonNull ProjectEntity projectEntity, @NonNull ProjectDTO projectDTO) {
        return null;
    }

    @Override
    public ProjectEntity starProject(@NonNull ProjectEntity projectEntity, @NonNull ProjectDTO projectDTO) {
        return null;
    }

    @Override
    public ProjectEntity loadProject(@NonNull ProjectEntity projectEntity, @NonNull ProjectDTO projectDTO) {
        return null;
    }

    @Override
    public ProjectEntity reloadProject(@NonNull ProjectEntity projectEntity, @NonNull ProjectDTO projectDTO) {
        return null;
    }

    @Override
    public ProjectEntity updateLastModifiedData(@NonNull ProjectEntity projectEntity, @NonNull ProjectDTO projectDTO) {
        return null;
    }

    @Override
    public ProjectEntity importProjectFromConfig(@NonNull ProjectEntity projectEntity, @NonNull ProjectConfigDTO projectConfigFileData) {
        return null;
    }

    @Override
    public ProjectEntity exportProjectConfig(@NonNull ProjectEntity projectEntity, @NonNull ProjectConfigDTO projectConfigFileData) {
        return null;
    }

    @Override
    public ProjectStatisticsDTO getProjectStatistics(@NonNull ProjectEntity projectEntity, @NonNull ProjectDTO projectDTO) {
        return null;
    }

    @Override
    public void updateLabels(@NonNull ProjectEntity projectEntity, @NonNull ProjectDTO projectDTO) {

    }

    @Override
    public boolean checkExistProject(@NonNull ProjectEntity projectEntity, @NonNull ProjectDTO projectDTO) {
        return false;
    }
}
