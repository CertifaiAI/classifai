package ai.classifai.core.service.project;

import ai.classifai.core.dto.ProjectDTO;
import ai.classifai.core.entity.project.Project;
import io.vertx.core.Future;
import lombok.NonNull;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository {
    Future<Project> createProject(@NonNull ProjectDTO projectDTO);

    Future<List<Project>> listProjects(@NonNull Integer annotationType);

    Future<Project> updateProject(@NonNull Project projectEntity, @NonNull ProjectDTO projectDTO);

    Future<Optional<Project>> getProjectById(@NonNull String projectId);

    Future<Void> deleteProjectById(@NonNull Project projectEntity);

}
