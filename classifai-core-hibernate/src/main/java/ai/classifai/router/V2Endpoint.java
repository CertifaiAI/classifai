/*
 * Copyright (c) 2021 CertifAI Sdn. Bhd.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 */
package ai.classifai.router;

import ai.classifai.database.DbActionConfig;
import ai.classifai.selector.project.LabelFileSelector;
import ai.classifai.selector.project.ProjectFolderSelector;
import ai.classifai.selector.status.FileSystemStatus;
import ai.classifai.selector.status.SelectionWindowStatus;
import ai.classifai.util.ParamConfig;
import ai.classifai.util.http.HTTPResponseHandler;
import ai.classifai.util.message.ReplyHandler;
import ai.classifai.util.type.AnnotationType;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;

/**
 * Classifai v2 endpoints
 *
 * @author devenyantis
 */
@Slf4j
public class V2Endpoint extends EndpointBase {

    @Setter private ProjectFolderSelector projectFolderSelector = null;
//    @Setter private ProjectImportSelector projectImporter = null;

    @Setter private LabelFileSelector labelFileSelector = null;

    // FIXME: when to set is_load is true??
    //  Need to have body handler for open and close state
    /***
     * change is_load state of a project to false
     *
     * PUT http://localhost:{port}/:annotation_type/projects/:project_name
     */
    public void closeProjectState(RoutingContext context)
    {
        JsonObject request = paramHandler.projectParamToJson(context);

        DeliveryOptions options = getDeliveryOptions(DbActionConfig.CLOSE_PROJECT_STATE);

        vertx.eventBus().request(DbActionConfig.QUEUE, request, options)
                .onSuccess(msg -> HTTPResponseHandler.configureOK(context, ReplyHandler.getOkReply()))
                .onFailure(throwable -> HTTPResponseHandler.configureOK(context,
                        ReplyHandler.reportUserDefinedError("Unable to close project state")));
    }

    /***
     * Star a project
     *
     * PUT http://localhost:{port}/:annotation_type/projects/:projectname/star
     */
    public void starProject(RoutingContext context)
    {
        JsonObject request = paramHandler.projectParamToJson(context);

        Handler<Buffer> requestHandler = h ->
        {
            JsonObject status = h.toJsonObject();

            request.mergeIn(status);

            DeliveryOptions options = getDeliveryOptions(DbActionConfig.STAR_PROJECT);

            vertx.eventBus().request(DbActionConfig.QUEUE, request, options)
                    .onSuccess(msg -> sendResponseBody(msg, context))
                    .onFailure(throwable -> HTTPResponseHandler.configureOK(context,
                            ReplyHandler.reportUserDefinedError("Unable to star project")));
        };

        context.request().bodyHandler(requestHandler);
    }

    /**
     * Create new project
     * PUT http://localhost:{port}/v2/projects
     *
     * Request Body
     * {
     *   "project_name": "test-project",
     *   "annotation_type": "boundingbox",
     *   "project_path": "/Users/codenamwei/Desktop/Education/books",
     *   "label_file_path": "/Users/codenamewei/Downloads/test_label.txt",
     * }
     *
     */
    public void createProject(RoutingContext context)
    {
        Handler<Buffer> requestHandler = h ->
        {
            JsonObject request = h.toJsonObject();
            String annoTypeKey = "annotation_type";
            AnnotationType type = bodyHandler.getAnnoType(request.getString(annoTypeKey));
            request.put(annoTypeKey, type.ordinal());

            DeliveryOptions createOptions = getDeliveryOptions(DbActionConfig.CREATE_PROJECT);

            vertx.eventBus().request(DbActionConfig.QUEUE, request, createOptions)
                    .onSuccess(msg -> sendResponseBody(msg, context))
                    .onFailure(throwable -> HTTPResponseHandler.configureOK(context,
                            ReplyHandler.reportUserDefinedError("Unable to create project")));
        };

        context.request().bodyHandler(requestHandler);
    }

    // TODO: to be deleted
    /**
     * Create new project status
     * GET http://localhost:{port}/v2/:annotation_type/projects/:project_name
     *
     * Example:
     * GET http://localhost:{port}/v2/bndbox/projects/helloworld
     */
    public void createProjectStatus(RoutingContext context)
    {
        JsonObject response = compileFileSysStatusResponse(FileSystemStatus.DATABASE_UPDATED);

        response.put(ParamConfig.getUnsupportedImageListParam(), new ArrayList<String>());

        HTTPResponseHandler.configureOK(context, response);
    }


    // TODO: suggestion => put new project name in body instead of param
    /**
     * Rename project
     * PUT http://localhost:{port}/v2/:annotation_type/:project_name/rename/:new_project_name
     *
     * Example:
     * PUT http://localhost:{port}/v2/bndbox/:project_name/rename/:new_project_name
     *
     */
    public void renameProject(RoutingContext context)
    {
        JsonObject request = paramHandler.renameParamToJson(context);

        DeliveryOptions options = getDeliveryOptions(DbActionConfig.RENAME_PROJECT);

        vertx.eventBus().request(DbActionConfig.QUEUE, request, options)
                .onSuccess(msg -> sendResponseBody(msg, context))
                .onFailure(throwable -> HTTPResponseHandler.configureOK(context,
                        ReplyHandler.reportUserDefinedError("Unable to rename project")));
    }

    /**
     * Reload v2 project
     * PUT http://localhost:{port}/v2/:annotation_type/projects/:project_name/reload
     *
     * Example:
     * PUT http://localhost:{port}/v2/bndbox/projects/helloworld/reload
     *
     */
    public void reloadProject(RoutingContext context)
    {
        JsonObject request = paramHandler.projectParamToJson(context);

        DeliveryOptions options = getDeliveryOptions(DbActionConfig.RELOAD_PROJECT);

        vertx.eventBus().request(DbActionConfig.QUEUE, request, options)
                .onSuccess(msg -> sendResponseBody(msg, context))
                .onFailure(throwable -> HTTPResponseHandler.configureOK(context,
                        ReplyHandler.reportUserDefinedError("Unable to reload project")));
    }


    // TODO: to be deleted
    /**
     * Get load status of project
     * GET http://localhost:{port}/v2/:annotation_type/projects/:project_name/reloadstatus
     *
     * Example:
     * GET http://localhost:{port}/v2/bndbox/projects/helloworld/reloadstatus
     *
     */
    public void reloadProjectStatus(RoutingContext context)
    {
        JsonObject res = compileFileSysStatusResponse(FileSystemStatus.DATABASE_UPDATED);

        res.put(ParamConfig.getUuidAdditionListParam(), new ArrayList<String>());
        res.put(ParamConfig.getUuidDeletionListParam(), new ArrayList<String>());
        res.put(ParamConfig.getUnsupportedImageListParam(), new ArrayList<String>());

        HTTPResponseHandler.configureOK(context, res);
    }
//
//    /***
//     * export a project to configuration file
//     *
//     * PUT http://localhost:{port}/v2/:annotation_type/projects/:project_name/export/:export_type
//     */
//    public void exportProject(RoutingContext context)
//    {
//        AnnotationType type = AnnotationHandler.getTypeFromEndpoint(context.request().getParam(ParamConfig.getAnnotationTypeParam()));
//
//        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());
//        String projectId = ProjectHandler.getProjectId(projectName, type.ordinal());
//
//        if(helper.checkIfProjectNull(context, projectId, projectName)) return;
//
//        ActionConfig.ExportType exportType = ProjectExport.getExportType(
//                context.request().getParam(ActionConfig.getExportTypeParam()));
//        if(exportType.equals(ActionConfig.ExportType.INVALID_CONFIG)) return;
//
//        JsonObject request = new JsonObject()
//                .put(ParamConfig.getProjectIdParam(), projectId)
//                .put(ParamConfig.getAnnotationTypeParam(), type.ordinal())
//                .put(ActionConfig.getExportTypeParam(), exportType.ordinal());
//
//        DeliveryOptions options = new DeliveryOptions().addHeader(ParamConfig.getActionKeyword(), PortfolioDbQuery.getExportProject());
//
//        // Initiate export status
//        ProjectExport.setExportStatus(ProjectExport.ProjectExportStatus.EXPORT_STARTING);
//        ProjectExport.setExportPath("");
//
//        vertx.eventBus().request(PortfolioDbQuery.getQueue(), request, options, reply -> {
//
//            if (reply.succeeded()) {
//
//                JsonObject response = (JsonObject) reply.result().body();
//
//                HTTPResponseHandler.configureOK(context, response);
//            }
//            else
//            {
//                HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError("Export of project failed for " + projectName));
//                ProjectExport.setExportStatus(ProjectExport.ProjectExportStatus.EXPORT_FAIL);
//            }
//        });
//
//    }
//
//    /**
//     * Get export project status
//     * GET http://localhost:{port}/v2/:annotation_type/projects/exportstatus
//     *
//     * Example:
//     * GET http://localhost:{port}/v2/bndbox/projects/exportstatus
//     *
//     */
//    public void getExportStatus(RoutingContext context)
//    {
//        helper.checkIfDockerEnv(context);
//
//        ProjectExport.ProjectExportStatus exportStatus = ProjectExport.getExportStatus();
//        JsonObject response = ReplyHandler.getOkReply();
//        response.put(ActionConfig.getExportStatusParam(), exportStatus.ordinal());
//        response.put(ActionConfig.getExportStatusMessageParam(), exportStatus.name());
//
//        if(exportStatus.equals(ProjectExport.ProjectExportStatus.EXPORT_SUCCESS))
//        {
//            response.put(ActionConfig.getProjectConfigPathParam(), ProjectExport.getExportPath());
//        }
//
//        HTTPResponseHandler.configureOK(context, response);
//    }
//
//    public void importProject(RoutingContext context)
//    {
//        if(projectImporter.isWindowOpen())
//        {
//            JsonObject jsonResonse = ReplyHandler.reportUserDefinedError("Import config file selector window has already opened. Close that to proceed.");
//
//            HTTPResponseHandler.configureOK(context, jsonResonse);
//        }
//        else
//        {
//            HTTPResponseHandler.configureOK(context);
//        }
//
//        projectImporter.run();
//    }
//
//    /**
//     * Get import project status
//     * GET http://localhost:{port}/v2/:annotation_type/projects/importstatus
//     *
//     * Example:
//     * GET http://localhost:{port}/v2/bndbox/projects/importstatus
//     *
//     */
//    public void getImportStatus(RoutingContext context)
//    {
//        helper.checkIfDockerEnv(context);
//
//        FileSystemStatus fileSysStatus = ProjectImportSelector.getImportFileSystemStatus();
//        JsonObject response = compileFileSysStatusResponse(fileSysStatus);
//
//        if(fileSysStatus.equals(FileSystemStatus.DATABASE_UPDATED))
//        {
//            response.put(ParamConfig.getProjectNameParam(), ProjectImportSelector.getProjectNameParam());
//        }
//
//        HTTPResponseHandler.configureOK(context, response);
//    }

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
