package ai.classifai.action;

import ai.classifai.database.model.Project;
import io.vertx.core.json.JsonObject;
import lombok.Data;

@Data
public class ProjectExporterImpl implements ProjectExporter
{
    private String exportPath;

    @Override
    public JsonObject getConfigTemplate()
    {
        JsonObject jsonObj = new JsonObject();
        jsonObj.put(ActionConfig.TOOL_PARAM, "classifai");
        jsonObj.put(ActionConfig.TOOL_VERSION_PARAM, this.getClass().getPackage().getImplementationVersion());

        return jsonObj;
    }

    @Override
    public void exportProject(Project project)
    {

    }

    @Override
    public JsonObject getProjectConfig(Project project)
    {
        JsonObject template = getConfigTemplate();

        return null;
    }
}
