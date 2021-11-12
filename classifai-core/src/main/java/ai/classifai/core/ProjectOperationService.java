package ai.classifai.core;

import ai.classifai.backend.action.LabelListImport;
import ai.classifai.backend.action.rename.RenameProjectData;
import ai.classifai.backend.database.annotation.AnnotationDB;
import ai.classifai.backend.database.portfolio.PortfolioDB;
import ai.classifai.backend.database.versioning.Version;
import ai.classifai.core.entities.dto.DeleteProjectDataDTO;
import ai.classifai.core.entities.dto.LabelListDTO;
import ai.classifai.core.entities.dto.RenameDataDTO;
import ai.classifai.core.entities.properties.ThumbnailProperties;
import ai.classifai.core.entities.response.DeleteProjectDataResponse;
import ai.classifai.core.entities.response.RenameDataResponse;
import ai.classifai.core.loader.ProjectLoader;
import ai.classifai.core.util.datetime.DateTime;
import ai.classifai.core.util.http.ActionStatus;
import ai.classifai.core.util.message.ReplyHandler;
import ai.classifai.core.util.project.ProjectHandler;
import ai.classifai.core.util.type.AnnotationType;
import io.vertx.core.Future;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.List;
import java.util.Objects;

@Slf4j
public class ProjectOperationService {
    @Getter private final PortfolioDB portfolioDB;
    @Getter private final AnnotationDB annotationDB;
    private final ProjectHandler projectHandler;
    private final LabelListImport labelListImport = new LabelListImport();

    public ProjectOperationService(PortfolioDB portfolioDB, ProjectHandler projectHandler, AnnotationDB annotationDB) {
        this.portfolioDB = portfolioDB;
        this.projectHandler = projectHandler;
        this.annotationDB = annotationDB;
    }

    public ProjectLoader getProjectLoader(String projectName, AnnotationType projectType) {
        return projectHandler.getProjectLoader(projectName, projectType);
    }

    public String getProjectId(String projectName, int typeOrdinal) {
        return projectHandler.getProjectId(projectName, typeOrdinal);
    }

    public boolean checkValidProjectRename(String projectName, int typeOrdinal){
        return projectHandler.checkValidProjectRename(projectName, typeOrdinal);
    }

    public Future<ActionStatus> getProjectMetadata(ProjectLoader loader, String projectName) {
        return portfolioDB.getProjectMetadata(loader.getProjectId())
                .map(ActionStatus::okWithResponse)
                .otherwise(cause -> ActionStatus.failedWithMessage("Failed to retrieve metadata for project " + projectName));
    }

    public Future<ActionStatus> getAllProjectsMeta(AnnotationType projectType) {
        return portfolioDB.getAllProjectsMeta(projectType.ordinal())
                .map(ActionStatus::okWithResponse)
                .otherwise(cause -> ActionStatus.failedWithMessage("Failure in getting all the projects for " + projectType.name()));
    }

    public void loadProjectLoader(ProjectLoader loader) {
        projectHandler.loadProjectLoader(loader);
    }

    public boolean isProjectNameUnique(String projectName, int annotationInt) {
        return projectHandler.isProjectNameUnique(projectName, annotationInt);
    }

    public Future<ActionStatus> renameProject(ProjectLoader loader, String projectName, String newProjectName) {
        return portfolioDB.renameProject(loader.getProjectId(), newProjectName)
                .map(result -> {
                    loader.setProjectName(newProjectName);
                    projectHandler.updateProjectNameInCache(loader.getProjectId(), loader, projectName);
                    log.debug("Rename to " + newProjectName + " success.");

                    return ActionStatus.ok();
                })
                .otherwise(ActionStatus.failedWithMessage("Failed to rename project " + projectName));
    }

    public Future<ActionStatus> reloadProject(ProjectLoader loader) {
        return portfolioDB.reloadProject(loader.getProjectId())
                .map(ActionStatus.ok())
                .otherwise(ActionStatus.failedWithMessage("Failed to reload project " + loader.getProjectName()));
    }

    public Future<ActionStatus> starProject(String projectID, Boolean isStarred) {
        return portfolioDB.starProject(projectID, isStarred)
                .map(result -> {
                    ProjectLoader loader = Objects.requireNonNull(projectHandler.getProjectLoader(projectID));
                    loader.setIsProjectStarred(isStarred);
                    return ActionStatus.ok();
                })
                .otherwise(ActionStatus.failedWithMessage("Star project fail"));
    }

    public Future<ActionStatus> updateData(ThumbnailProperties requestBody, ProjectLoader loader){
        return portfolioDB.updateData(requestBody, loader.getProjectId())
                .map(result -> {
                    updateLastModifiedDate(loader);
                    return ActionStatus.ok();
                })
                .otherwise(ActionStatus.failedWithMessage("Failure in updating database for " + loader.getAnnotationType()
                        + " project: " + loader.getProjectName()));
    }

    public List<String> getValidLabelList(File labelFile) {
        return labelListImport.getValidLabelList(labelFile);
    }

    private void updateLastModifiedDate(ProjectLoader loader)
    {
        String projectID = loader.getProjectId();

        Version version = loader.getProjectVersion().getCurrentVersion();

        version.setLastModifiedDate(new DateTime());

        portfolioDB.updateLastModifiedDate(projectID, version.getDbFormat())
                .onFailure(cause -> log.info("Databse update fail. Type: " + loader.getAnnotationType() + " Project: " + loader.getProjectName()));
    }

    public Future<ActionStatus> updateLabels(LabelListDTO requestBody, String projectID, String projectName) {
        return portfolioDB.updateLabels(projectID, requestBody.getLabelList())
                .map(ActionStatus.ok())
                .otherwise(ActionStatus.failedWithMessage("Fail to update labels: " + projectName));
    }

    public Future<DeleteProjectDataResponse> deleteProjectData(DeleteProjectDataDTO requestBody, String projectID){
        return portfolioDB.deleteProjectData(projectID, requestBody.getUuidList(), requestBody.getImgPathList())
                .map(result -> DeleteProjectDataResponse.builder()
                        .message(ReplyHandler.SUCCESSFUL)
                        .uuidList(result)
                        .build())
                .otherwise(DeleteProjectDataResponse.builder()
                        .message(ReplyHandler.FAILED)
                        .errorMessage("Delete project data fail")
                        .build());
    }

    public Future<RenameDataResponse> renameData(RenameDataDTO requestBody, String projectId) {
        return portfolioDB.renameData(projectId, requestBody.getUuid(), requestBody.getNewFilename())
                .map(result -> RenameDataResponse.builder()
                        .message(ReplyHandler.SUCCESSFUL)
                        .imgPath(result)
                        .build())
                .otherwise(cause -> RenameProjectData.reportRenameError(cause.getMessage()));
    }

    public Future<ActionStatus> loadProject(ProjectLoader loader) {
        return portfolioDB.loadProject(loader.getProjectId())
                .map(ActionStatus.ok())
                .otherwise(ActionStatus.failedWithMessage("Failed to load project " + loader.getProjectName() + ". Check validity of data points failed."));
    }

    public Future<ActionStatus> deleteProjectFromPortfolioDb(String projectName, AnnotationType type) {
        ProjectLoader loader = projectHandler.getProjectLoader(projectName, type);
        String projectID = loader.getProjectId();
        return portfolioDB.deleteProjectFromPortfolioDb(projectID)
                .compose(result -> portfolioDB.deleteProjectFromAnnotationDb(projectID))
                .map(result -> {
                    projectHandler.deleteProjectFromCache(projectID);
                    return ActionStatus.ok();
                })
                .onFailure(cause -> ActionStatus.failedWithMessage("Failure in delete project name: " + loader.getProjectName() + " for " + type.name()));
    }

}
