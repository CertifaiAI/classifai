package ai.classifai.database.handler;

import ai.classifai.action.LabelListImport;
import ai.classifai.database.model.Label;
import ai.classifai.database.model.Project;
import ai.classifai.database.model.data.Data;
import ai.classifai.util.data.DataHandler;
import ai.classifai.util.project.ProjectInfra;
import io.vertx.core.json.JsonObject;

import java.io.File;
import java.util.List;

public class ProjectHandler
{
    // create new project
    public static Project buildNewProject(String projectName, int annoType, String projectPath, String labelPath)
    {
        Project project = new Project(projectName, annoType, projectPath, true,
                false, ProjectInfra.ON_PREMISE.ordinal());

        // create new Data
        DataHandler dataHandler = DataHandler.getDataHandler(annoType);
        List<Data> dataList = dataHandler.getDataList(project);

        dataList.forEach(project::addData);

        // process label path get label list
        LabelHandler labelHandler = new LabelHandler();
        List<String> strLabelList = new LabelListImport(new File(labelPath)).getValidLabelList();
        List<Label> labelList = labelHandler.getLabelList(project, strLabelList);

        labelList.forEach(project.getCurrentVersion()::addLabel);

        return project;
    }
}
