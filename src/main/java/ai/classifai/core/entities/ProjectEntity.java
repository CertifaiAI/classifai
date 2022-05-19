package ai.classifai.core.entities;

import ai.classifai.data.AnnotationType;
import ai.classifai.data.ProjectInfra;
import ai.classifai.database.status.FileSystemStatus;
import ai.classifai.database.status.ProjectLoadStatus;

import java.util.List;

public interface ProjectEntity {
    String getProjectId();

    String getProjectName();

    String getProjectPath();

    AnnotationType getProjectType();

    ProjectInfra getProjectInfa();

    List<String> getLabelLists();

    FileSystemStatus getFileSystemStatus();

    ProjectLoadStatus getProjectLoadStatus();
}
