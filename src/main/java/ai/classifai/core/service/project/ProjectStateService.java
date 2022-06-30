package ai.classifai.core.service.project;

import ai.classifai.core.dto.ProjectDTO;
import ai.classifai.core.entity.project.ProjectEntity;
import lombok.NonNull;

public interface ProjectStateService {
    ProjectEntity updateLastModifiedData(@NonNull ProjectEntity projectEntity, @NonNull ProjectDTO projectDTO);

    ProjectEntity starProject(@NonNull ProjectEntity projectEntity, @NonNull ProjectDTO projectDTO);

    boolean checkExistProject(@NonNull ProjectEntity projectEntity, @NonNull ProjectDTO projectDTO);
}
