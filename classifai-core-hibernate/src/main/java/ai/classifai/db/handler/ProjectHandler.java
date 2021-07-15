package ai.classifai.db.handler;

import ai.classifai.action.LabelListImport;
import ai.classifai.db.entities.LabelEntity;
import ai.classifai.db.entities.ProjectEntity;
import ai.classifai.db.entities.data.DataEntity;
import ai.classifai.util.data.DataHandler;
import ai.classifai.util.project.ProjectInfra;

import java.io.File;
import java.util.List;

public class ProjectHandler
{
    // create new project
    public static ProjectEntity buildNewProject(String projectName, int annoType, String projectPath, String labelPath)
    {
        ProjectEntity project = new ProjectEntity(projectName, annoType, projectPath, true,
                false, ProjectInfra.ON_PREMISE.ordinal());

        // create new Data
        DataHandler dataHandler = DataHandler.getDataHandler(annoType);
        List<DataEntity> dataEntityList = dataHandler.getDataList(project);

        dataEntityList.forEach(project::addData);

        // process label path get label list
        LabelHandler labelHandler = new LabelHandler();
        List<String> strLabelList = new LabelListImport(new File(labelPath)).getValidLabelList();
        List<LabelEntity> labelEntityList = labelHandler.getLabelList(project, strLabelList);

        labelEntityList.forEach(project.getCurrentVersionEntity()::addLabel);

        return project;
    }
}
