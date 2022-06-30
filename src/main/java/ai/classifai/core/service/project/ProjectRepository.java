package ai.classifai.core.service.project;

import ai.classifai.core.dto.ProjectDTO;
import ai.classifai.core.entity.project.Project;
import ai.classifai.core.loader.ProjectLoader;
import io.vertx.core.Future;
import lombok.NonNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ProjectRepository {
    Future<Project> createProject(@NonNull ProjectDTO projectDTO);

    Future<Optional<List<Project>>> listProjects(@NonNull Integer annotationType);

    Future<Project> updateProject(@NonNull Project projectEntity, @NonNull ProjectDTO projectDTO);

    Future<Optional<Project>> getProjectById(@NonNull String projectId, @NonNull Integer annotationType);

    Future<Void> deleteProjectById(@NonNull Project projectEntity);

    Future<Void> renameProject();

    Future<Map<Integer, List<ProjectLoader>>> configProjectLoaderFromDb();

    Future<Void> updateUuidVersionList(@NonNull ProjectLoader projectLoader);

    Future<Void> startProject(@NonNull String projectID, @NonNull Boolean isStarred);

    Future<Void> updateLastModifiedDate(String projectId, String dbFormat);

    Future<Void> updateLabels(@NonNull String projectID, @NonNull List<String> labelList);

    Future<Void> updateIsNewParam(@NonNull String projectID);
}
