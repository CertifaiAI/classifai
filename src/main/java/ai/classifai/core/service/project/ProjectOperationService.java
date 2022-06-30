package ai.classifai.core.service.project;

import ai.classifai.core.dto.ProjectDTO;
import ai.classifai.core.entity.project.ProjectEntity;
import lombok.NonNull;

import java.io.IOException;

public interface ProjectOperationService {
    ProjectEntity importProjectFromConfig(@NonNull ProjectEntity projectEntity, @NonNull ProjectDTO projectConfigFileData) throws IOException;

    ProjectEntity exportProjectConfig(@NonNull ProjectEntity projectEntity, @NonNull ProjectDTO projectConfigFileData);
}
