package ai.classifai.router.controller;

import ai.classifai.selector.project.LabelFileSelector;
import ai.classifai.selector.project.ProjectFolderSelector;
import ai.classifai.selector.project.ProjectImportSelector;
import ai.classifai.selector.status.SelectionWindowStatus;
import ai.classifai.util.ParamConfig;
import ai.classifai.util.http.HTTPResponseHandler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;

public class SystemController extends AbstractVertxController
{
    private ProjectFolderSelector projectFolderSelector;
    private ProjectImportSelector projectImporter;
    private LabelFileSelector labelFileSelector;

    public SystemController(Vertx vertx, ProjectFolderSelector projectFolderSelector,
                            ProjectImportSelector projectImporter,
                            LabelFileSelector labelFileSelector)
    {
        super(vertx);
        this.projectFolderSelector = projectFolderSelector;
        this.projectImporter = projectImporter;
        this.labelFileSelector = labelFileSelector;
    }

    /**
     * Initiate load label list
     * PUT http://localhost:{port}/v2/labelfiles
     *
     * Example:
     * PUT http://localhost:{port}/v2/labelfiles
     */
    public void selectLabelFile(RoutingContext context)
    {
        helper.checkIfDockerEnv(context);

        if(!labelFileSelector.isWindowOpen())
        {
            labelFileSelector.run();

        }

        HTTPResponseHandler.configureOK(context);
    }

    /**
     * Get load label file status
     * GET http://localhost:{port}/v2/labelfiles
     *
     * Example:
     * GET http://localhost:{port}/v2/labelfiles
     */
    public void selectLabelFileStatus(RoutingContext context)
    {
        helper.checkIfDockerEnv(context);

        SelectionWindowStatus status = labelFileSelector.getWindowStatus();

        JsonObject jsonResponse = compileSelectionWindowResponse(status);

        if(status.equals(SelectionWindowStatus.WINDOW_CLOSE))
        {
            jsonResponse.put(ParamConfig.getLabelPathParam(), labelFileSelector.getLabelFilePath());
        }

        HTTPResponseHandler.configureOK(context, jsonResponse);
    }


    /**
     * Open folder selector to choose project folder
     * PUT http://localhost:{port}/v2/folders
     *
     * Example:
     * PUT http://localhost:{port}/v2/folders
     */
    public void selectProjectFolder(RoutingContext context)
    {
        helper.checkIfDockerEnv(context);

        if(!projectFolderSelector.isWindowOpen())
        {
            projectFolderSelector.run();

        }
        HTTPResponseHandler.configureOK(context);
    }

    /**
     * Get status of choosing a project folder
     * GET http://localhost:{port}/v2/folders
     *
     * Example:
     * GET http://localhost:{port}/v2/folders
     */
    public void selectProjectFolderStatus(RoutingContext context)
    {
        helper.checkIfDockerEnv(context);

        SelectionWindowStatus status = projectFolderSelector.getWindowStatus();

        JsonObject jsonResponse = compileSelectionWindowResponse(status);

        if(status.equals(SelectionWindowStatus.WINDOW_CLOSE))
        {
            jsonResponse.put(ParamConfig.getProjectPathParam(), projectFolderSelector.getProjectFolderPath());
        }

        HTTPResponseHandler.configureOK(context, jsonResponse);
    }
}
