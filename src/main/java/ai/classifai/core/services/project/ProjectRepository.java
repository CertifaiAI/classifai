package ai.classifai.core.services.project;

import ai.classifai.backend.dto.ProjectDTO;
import ai.classifai.backend.repository.entity.project.ProjectEntity;
import io.vertx.core.Future;
import lombok.NonNull;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository {
    Future<ProjectEntity> createProject(@NonNull ProjectDTO projectDTO);

    Future<List<ProjectEntity>> listProjects(Integer projectType);

    Future<ProjectEntity> updateProject(@NonNull ProjectEntity projectEntity, @NonNull ProjectDTO projectDTO);

    Future<Optional<ProjectEntity>> getProjectById(@NonNull String projectId);

    Future<Void> deleteProjectById(@NonNull ProjectEntity projectEntity);

    ProjectEntity toProjectEntity(@NonNull ProjectDTO projectDTO);
}
