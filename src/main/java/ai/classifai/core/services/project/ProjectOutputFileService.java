package ai.classifai.core.services.project;

import ai.classifai.backend.dto.ProjectInfoDTO;

public interface ProjectOutputFileService {
    void saveProjectFile(ProjectInfoDTO projectEntity);
}
