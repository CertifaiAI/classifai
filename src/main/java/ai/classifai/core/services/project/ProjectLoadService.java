package ai.classifai.core.services.project;

import ai.classifai.dto.ProjectInfoDTO;
import ai.classifai.repository.project.ProjectEntity;
import lombok.NonNull;

public interface ProjectLoadService {
    ProjectEntity loadProject(@NonNull ProjectEntity projectEntity, @NonNull ProjectInfoDTO projectDTO);

    ProjectEntity reloadProject(@NonNull ProjectEntity projectEntity, @NonNull ProjectInfoDTO projectDTO);
}
