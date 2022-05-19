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
    private final List<String> labelList;
    private final FileSystemStatus fileSystemStatus;
    private final ProjectLoadStatus projectLoadStatus;

    Project(String projectName, String projectId, String projectPath, AnnotationType projectType,
            List<String> labelList, ProjectInfra projectInfra, FileSystemStatus fileSystemStatus,
            ProjectLoadStatus projectLoadStatus) {
        this.projectName = projectName;
        this.projectId = projectId;
        this.projectPath = projectPath;
        this.projectType = projectType;
        this.labelList = labelList;
        this.projectInfra = projectInfra;
        this.fileSystemStatus = fileSystemStatus;
        this.projectLoadStatus = projectLoadStatus;
    }

    @Override
    public String getProjectId() {
        return projectId;
    }

    @Override
    public String getProjectName() {
        return projectName;
    }

    @Override
    public String getProjectPath() { return projectPath; }

    @Override
    public List<String> getLabelLists() { return labelList; }

    @Override
    public FileSystemStatus getFileSystemStatus() { return fileSystemStatus; }

    @Override
    public ProjectLoadStatus getProjectLoadStatus() { return projectLoadStatus; }

    @Override
    public AnnotationType getProjectType() {
        return projectType;
    }

    @Override
    public ProjectInfra getProjectInfa() {
        return projectInfra;
    }

    @Override
    public String toString() {
        return "Project{" +
                "projectName='" + projectName + '\'' +
                ", projectId='" + projectId + '\'' +
                ", projectType=" + projectType +
                ", projectPath='" + projectPath + '\'' +
                ", projectInfra=" + projectInfra +
                ", labelList=" + labelList +
                ", fileSystemStatus=" + fileSystemStatus +
                ", projectLoadStatus=" + projectLoadStatus +
                '}';
    }
}
