package ai.classifai.database.project;

import ai.classifai.data.AnnotationType;
import ai.classifai.data.ProjectInfra;
import ai.classifai.database.status.FileSystemStatus;
import ai.classifai.database.status.ProjectLoadStatus;

import java.util.List;

public class Project implements ProjectStatus {
    private String projectName;
    private String projectId;
    private AnnotationType projectType;
    private String projectPath;
    private List<String> labelList;
    private ProjectInfra projectInfra;
    FileSystemStatus fileSystemStatus;
    ProjectLoadStatus projectLoadStatus;

    Project(String projectName, String projectId, String projectPath, AnnotationType projectType,
            List<String> labelList, ProjectInfra projectInfra) {
        this.projectName = projectName;
        this.projectId = projectId;
        this.projectPath = projectPath;
        this.projectType = projectType;
        this.labelList = labelList;
        this.projectInfra = projectInfra;
    }

    public FileSystemStatus getFileSystemStatus() {
        return this.fileSystemStatus;
    }

    public void setFileSystemStatus(FileSystemStatus fileSystemStatus) {
        this.fileSystemStatus = fileSystemStatus;
    }

    public ProjectLoadStatus getProjectLoadStatus() {
        return this.projectLoadStatus;
    }

    public void setProjectLoadStatus(ProjectLoadStatus projectLoadStatus) {
        this.projectLoadStatus = projectLoadStatus;
    }
}
