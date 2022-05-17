package ai.classifai.database.project;

import ai.classifai.core.entities.ProjectEntity;
import ai.classifai.data.AnnotationType;
import ai.classifai.data.ProjectInfra;
import ai.classifai.database.status.FileSystemStatus;
import ai.classifai.database.status.ProjectLoadStatus;

import java.util.List;

public class Project implements ProjectEntity {
    private final String projectName;
    private final String projectId;
    private final AnnotationType projectType;
    private final String projectPath;
    private final ProjectInfra projectInfra;
    private List<String> labelList;
    private FileSystemStatus fileSystemStatus;
    private ProjectLoadStatus projectLoadStatus;

    Project(String projectName, String projectId, String projectPath, AnnotationType projectType,
            List<String> labelList, ProjectInfra projectInfra) {
        this.projectName = projectName;
        this.projectId = projectId;
        this.projectPath = projectPath;
        this.projectType = projectType;
        this.labelList = labelList;
        this.projectInfra = projectInfra;
    }

    @Override
    public String getProjectId() {
        return this.projectId;
    }

    @Override
    public String getProjectName() {
        return this.projectName;
    }

    @Override
    public String getProjectPath() {
        return this.projectPath;
    }

    @Override
    public AnnotationType getProjectType() {
        return this.projectType;
    }

    @Override
    public ProjectInfra getProjectInfa() {
        return this.projectInfra;
    }
}
