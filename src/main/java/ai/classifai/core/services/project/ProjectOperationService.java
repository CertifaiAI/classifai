package ai.classifai.core.services.project;

import ai.classifai.backend.dto.ProjectConfigDTO;
import ai.classifai.backend.repository.entity.project.ProjectEntity;
import lombok.NonNull;

import java.io.IOException;

public interface ProjectOperationService {
    ProjectEntity importProjectFromConfig(@NonNull ProjectEntity projectEntity, @NonNull ProjectConfigDTO projectConfigFileData) throws IOException;

    ProjectEntity exportProjectConfig(@NonNull ProjectEntity projectEntity, @NonNull ProjectConfigDTO projectConfigFileData);
}
