package ai.classifai.core.services.project;

import ai.classifai.core.entities.ProjectEntity;
import ai.classifai.dto.ProjectDTO;
import lombok.NonNull;

public interface ProjectLoadService {
    ProjectEntity loadProject(@NonNull ProjectEntity projectEntity, @NonNull ProjectDTO projectDTO);

    ProjectEntity reloadProject(@NonNull ProjectEntity projectEntity, @NonNull ProjectDTO projectDTO);
}
