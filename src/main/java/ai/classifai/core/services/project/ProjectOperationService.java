package ai.classifai.core.services.project;

import ai.classifai.core.entities.ProjectEntity;
import ai.classifai.dto.ProjectConfigDTO;
import lombok.NonNull;

import java.io.IOException;

public interface ProjectOperationService {
    ProjectEntity importProjectFromConfig(@NonNull ProjectEntity projectEntity, @NonNull ProjectConfigDTO projectConfigFileData) throws IOException;

    ProjectEntity exportProjectConfig(@NonNull ProjectEntity projectEntity, @NonNull ProjectConfigDTO projectConfigFileData);
}
