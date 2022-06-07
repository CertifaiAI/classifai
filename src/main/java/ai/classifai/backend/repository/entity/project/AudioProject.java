package ai.classifai.backend.repository.entity.project;

import ai.classifai.backend.dto.ProjectDTO;

public final class AudioProject extends ProjectEntity {
    private String audioFilePath;

    AudioProject(ProjectDTO projectDTO, String audioFilePath)
    {
        super(projectDTO);
        this.audioFilePath = audioFilePath;
    }
}
