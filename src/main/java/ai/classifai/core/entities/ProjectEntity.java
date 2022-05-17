package ai.classifai.core.entities;

import ai.classifai.data.AnnotationType;
import ai.classifai.data.ProjectInfra;

public interface ProjectEntity {
    String getProjectId();

    String getProjectName();

    String getProjectPath();

    AnnotationType getProjectType();

    ProjectInfra getProjectInfa();
}
