package ai.classifai.core.service.project;

import ai.classifai.core.dto.ProjectDTO;
import ai.classifai.core.loader.ProjectLoader;
import io.vertx.core.Future;
import lombok.NonNull;

import java.util.List;
import java.util.Optional;

public interface ProjectService {

    Future<ProjectDTO> createProject(@NonNull ProjectDTO projectDTO);

    Future<Optional<List<ProjectDTO>>> listProjects(@NonNull Integer projectType);

    Future<Optional<ProjectDTO>> getProjectById(@NonNull ProjectLoader loader);

    Future<ProjectDTO> updateProject(@NonNull ProjectDTO projectDTO);

    Future<Void> deleteProject(@NonNull ProjectDTO projectDTO);

    Future<Void> updateUuidVersionList(@NonNull ProjectLoader projectLoader);

    Future<Void> starProject(@NonNull String projectID, @NonNull Boolean isStarred);

    Future<Void> updateLabels(@NonNull String projectID, @NonNull List<String> labelList);

    Future<Void> updateLastModifiedDate(@NonNull String projectID, @NonNull String dbFormat);

    Future<Void> updateIsNewParam(@NonNull String projectID);
}
