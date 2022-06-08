package ai.classifai.core.service.project;

import ai.classifai.core.dto.ProjectDTO;
import ai.classifai.core.entity.project.ProjectEntity;
import io.vertx.core.Future;
import lombok.NonNull;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository {
    Future<ProjectEntity> createProject(@NonNull ProjectDTO projectDTO);

    Future<List<ProjectEntity>> listProjects(@NonNull Integer projectType);

    Future<ProjectEntity> updateProject(@NonNull ProjectEntity projectEntity, @NonNull ProjectDTO projectDTO);

    Future<Optional<ProjectEntity>> getProjectById(@NonNull String projectId);

    Future<Void> deleteProjectById(@NonNull ProjectEntity projectEntity);

    ProjectEntity toProjectEntity(@NonNull ProjectDTO projectDTO);
}
