package ai.classifai.core;

import ai.classifai.backend.action.ProjectExport;
import ai.classifai.backend.database.portfolio.PortfolioDB;
import ai.classifai.core.enums.ExportType;
import ai.classifai.core.enums.ProjectExportStatus;
import ai.classifai.core.util.http.ActionStatus;
import ai.classifai.core.util.project.ProjectHandler;
import io.vertx.core.Future;

public class ExportProjectService {
    private final PortfolioDB portfolioDB;
    private final ProjectHandler projectHandler;
    private final ProjectExport projectExport;

    public ExportProjectService(PortfolioDB portfolioDB, ProjectHandler projectHandler, ProjectExport projectExport) {
        this.portfolioDB = portfolioDB;
        this.projectHandler = projectHandler;
        this.projectExport = projectExport;
    }

    public String getProjectId(String projectName, int projectType) {
        return projectHandler.getProjectId(projectName, projectType);
    }

    public ExportType getExportType(String exportTypeVar) {
        return projectExport.getExportType(exportTypeVar);
    }

    public void setExportStatus(ProjectExportStatus status) {
        projectExport.setExportStatus(status);
    }

    public ProjectExportStatus getExportStatus() {
        return projectExport.getExportStatus();
    }

    public String getExportPath() {
        return projectExport.getExportPath();
    }

    public Future<ActionStatus> exportProject(String projectId, int exportTypeOrdinal, String projectName) {
        projectExport.setExportStatus(ProjectExportStatus.EXPORT_STARTING);
        return portfolioDB.exportProject(projectId, exportTypeOrdinal)
                .map(ActionStatus.ok())
                .otherwise(ActionStatus.failedWithMessage("Export of project failed for " + projectName));
    }
}
