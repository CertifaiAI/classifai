package ai.classifai.core.services.project;

import ai.classifai.core.entities.ProjectEntity;
import ai.classifai.dto.ProjectDTO;
import lombok.NonNull;

public interface ProjectStateService {
    ProjectEntity updateLastModifiedData(@NonNull ProjectEntity projectEntity, @NonNull ProjectDTO projectDTO);

    ProjectEntity starProject(@NonNull ProjectEntity projectEntity, @NonNull ProjectDTO projectDTO);

    boolean checkExistProject(@NonNull ProjectEntity projectEntity, @NonNull ProjectDTO projectDTO);
}
