package ai.classifai.core.services.project;

import ai.classifai.core.entities.ProjectEntity;
import ai.classifai.dto.ProjectDTO;
import lombok.NonNull;

import java.util.List;

public interface ProjectDataService {
    List<ProjectEntity> getAllProjectMeta();

    ProjectDTO getProjectMetaById(@NonNull String projectId);
}
