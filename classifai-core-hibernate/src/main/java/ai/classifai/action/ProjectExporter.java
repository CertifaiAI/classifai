package ai.classifai.action;

import ai.classifai.database.model.Project;
import io.vertx.core.json.JsonObject;

public interface ProjectExporter
{
    JsonObject getConfigTemplate();

    void exportProject(Project project);

    String getExportPath();

    JsonObject getProjectConfig(Project project);
}
