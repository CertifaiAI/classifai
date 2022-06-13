package ai.classifai.core.service.project;

import ai.classifai.core.dto.ProjectDTO;
import io.vertx.core.Future;
import lombok.NonNull;

import java.util.List;
import java.util.Optional;

public interface ProjectService {

    Future<ProjectDTO> createProject(@NonNull ProjectDTO projectDTO);

    Future<List<ProjectDTO>> listProjects(@NonNull Integer projectType);

    Future<ProjectDTO> updateProject(@NonNull ProjectDTO projectDTO);

    Future<Void> deleteProject(@NonNull ProjectDTO projectDTO);

    Future<Optional<ProjectDTO>> getProjectByNameAndType(@NonNull ProjectDTO projectDTO);

    Future<Optional<ProjectDTO>> getProjectById(@NonNull String projectName, @NonNull Integer projectType);

}
