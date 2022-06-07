package ai.classifai.core.services.project;

import ai.classifai.backend.dto.ProjectInfoDTO;
import ai.classifai.backend.repository.entity.project.ProjectEntity;
import lombok.NonNull;

public interface ProjectStateService {
    ProjectEntity updateLastModifiedData(@NonNull ProjectEntity projectEntity, @NonNull ProjectInfoDTO projectDTO);

    ProjectEntity starProject(@NonNull ProjectEntity projectEntity, @NonNull ProjectInfoDTO projectDTO);

    boolean checkExistProject(@NonNull ProjectEntity projectEntity, @NonNull ProjectInfoDTO projectDTO);
}
