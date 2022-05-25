package ai.classifai.repository.project;

import ai.classifai.dto.ProjectDTO;

public final class AudioProject extends ProjectEntity {
    private String audioFilePath;

    AudioProject(ProjectDTO projectDTO, String audioFilePath)
    {
        super(projectDTO);
        this.audioFilePath = audioFilePath;
    }
}
