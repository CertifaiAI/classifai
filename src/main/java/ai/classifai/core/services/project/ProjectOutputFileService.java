package ai.classifai.core.services.project;

import ai.classifai.dto.ProjectInfoDTO;

public interface ProjectOutputFileService {
    void saveProjectFile(ProjectInfoDTO projectEntity);
}
