package ai.classifai.database.project;

import ai.classifai.database.status.FileSystemStatus;
import ai.classifai.database.status.ProjectLoadStatus;

public interface ProjectStatus {
    FileSystemStatus getFileSystemStatus();
    void setFileSystemStatus(FileSystemStatus fileSystemStatus);
    ProjectLoadStatus getProjectLoadStatus();
    void setProjectLoadStatus(ProjectLoadStatus projectLoadStatus);
}
