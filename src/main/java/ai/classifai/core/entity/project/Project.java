package ai.classifai.core.entity.project;

import ai.classifai.core.enumeration.ProjectInfra;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class Project implements ProjectEntity {

    String projectName;

    String projectId;

    String projectPath;

    Integer annotationType;

    String projectInfra;

    Boolean isNew;

    Boolean isStarred;

    Boolean isLoaded;

    Boolean isRootPathValid;

    String currentVersion;

    String versionUuid;

    List<String> labelList;

    Integer totalUuid;

    String lastModifiedDate;

    String createdDate;

    String uuidProjectVersion;

    String labelProjectVersion;

    String versionList;

    @Override
    public String getProjectName() {
        return projectName;
    }

    @Override
    public String getProjectId() {
        return projectId;
    }

    @Override
    public Integer getAnnotationType() {
        return annotationType;
    }

    @Override
    public String getProjectPath() {
        return projectPath;
    }

    @Override
    public ProjectInfra getProjectInfra() {
        return ProjectInfra.get(projectInfra);
    }

    @Override
    public Boolean getIsProjectNew() {
        return isNew;
    }

    @Override
    public Boolean getIsProjectStarred() {
        return isStarred;
    }

    @Override
    public Boolean getIsProjectLoaded() {
        return isLoaded;
    }

    @Override
    public Boolean getIsCloud() {
        return false;
    }

    @Override
    public String getCreatedDate() {
        return createdDate;
    }

    @Override
    public String getCurrentVersion() {
        return currentVersion;
    }

    @Override
    public String getLastModifiedDate() {
        return lastModifiedDate;
    }

    @Override
    public String getVersionUuid() {
        return versionUuid;
    }

    @Override
    public Integer getExistingDataInDir() {
        return totalUuid;
    }

    @Override
    public Boolean getIsRootPathValidParam() {
        return isRootPathValid;
    }

    @Override
    public List<String> getLabelList() {
        return labelList;
    }
}
