package ai.classifai.repository.service;

import ai.classifai.dto.ProjectDTO;
import ai.classifai.repository.project.ProjectEntity;
import io.vertx.core.Future;
import lombok.NonNull;

import java.util.List;

public interface ProjectRepository {
    Future<ProjectDTO> createProject(@NonNull ProjectEntity projectEntity);

    List<ProjectDTO> listProjects();

    ProjectDTO updateProject(@NonNull ProjectEntity projectEntity);

    ProjectDTO getProjectById(@NonNull ProjectEntity projectEntity);

    ProjectDTO deleteProjectById(@NonNull ProjectEntity projectEntity);

    ProjectDTO toProjectDTO(@NonNull ProjectEntity projectEntity);
}
