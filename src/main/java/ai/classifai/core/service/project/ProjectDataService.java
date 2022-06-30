package ai.classifai.core.service.project;

import ai.classifai.core.dto.ProjectDTO;
import ai.classifai.core.loader.ProjectLoader;
import ai.classifai.core.loader.ProjectLoaderStatus;
import io.vertx.core.Future;
import lombok.NonNull;

public interface ProjectDataService {
    Future<ProjectLoader> parseFileData(@NonNull ProjectDTO projectDTO) throws Exception;

    Future<Void> deleteProject(@NonNull ProjectDTO projectDTO);

    Future<ProjectLoaderStatus> loadProject(@NonNull ProjectLoader projectLoader);

}
