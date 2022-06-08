package ai.classifai.core.service.project;

import ai.classifai.core.dto.ProjectInfoDTO;
import ai.classifai.core.entity.project.ProjectEntity;
import lombok.NonNull;

public interface ProjectStateService {
    ProjectEntity updateLastModifiedData(@NonNull ProjectEntity projectEntity, @NonNull ProjectInfoDTO projectDTO);

    ProjectEntity starProject(@NonNull ProjectEntity projectEntity, @NonNull ProjectInfoDTO projectDTO);

    boolean checkExistProject(@NonNull ProjectEntity projectEntity, @NonNull ProjectInfoDTO projectDTO);
}
