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

import ai.classifai.action.ActionConfig;
import ai.classifai.action.ActionOps;
import ai.classifai.database.portfolio.PortfolioDbQuery;
import ai.classifai.loader.ProjectLoader;
import ai.classifai.selector.filesystem.FileSystemStatus;
import ai.classifai.selector.project.ProjectFolderSelector;
import ai.classifai.selector.project.ProjectImportSelector;
import ai.classifai.util.ParamConfig;
import ai.classifai.util.http.HTTPResponseHandler;
import ai.classifai.util.message.ErrorCodes;
import ai.classifai.util.message.ReplyHandler;
import ai.classifai.util.project.ProjectHandler;
import ai.classifai.util.type.AnnotationHandler;
import ai.classifai.util.type.AnnotationType;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Classifai v2 endpoints
 *
 * @author devenyantis
 */
@Slf4j
public class V2Endpoint {

    @Setter private Vertx vertx = null;
    @Setter private ProjectFolderSelector projectFolderSelector = null;
    @Setter private ProjectImportSelector projectImporter = null;

    Util util = new Util();

    /***
     * change is_load state of a project to false
     *
     * PUT http://localhost:{port}/:annotation_type/projects/:project_name
     */
    public void closeProjectState(RoutingContext context)
    {
        AnnotationType type = AnnotationHandler.getTypeFromEndpoint(context.request().getParam(ParamConfig.getAnnotationTypeParam()));

        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());
        String projectID = ProjectHandler.getProjectId(projectName, type.ordinal());

        if(util.checkIfProjectNull(context, projectID, projectName)) return;

        context.request().bodyHandler(h ->
        {
            try
            {
                JsonObject jsonObject = h.toJsonObject();

                if(jsonObject.getString(ParamConfig.getStatusParam()).equals("closed"))
                {
                    ProjectHandler.getProjectLoader(projectID).setIsLoadedFrontEndToggle(Boolean.FALSE);
                }
                else
                {
                    throw new IllegalArgumentException("Request payload failed to satisfied the status of {\"status\": \"closed\"} for " + projectName + ". ");
                }

                HTTPResponseHandler.configureOK(context);

            }
            catch (Exception e)
            {
                HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError(e.getMessage()));

            }
        });
    }

    /***
     * Star a project
     *
     * PUT http://localhost:{port}/:annotation_type/projects/:projectname/star
     */
    public void starProject(RoutingContext context)
    {
        AnnotationType type = AnnotationHandler.getTypeFromEndpoint(context.request().getParam(ParamConfig.getAnnotationTypeParam()));

        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());
        String projectID = ProjectHandler.getProjectId(projectName, type.ordinal());

        if(util.checkIfProjectNull(context, projectID, projectName)) return;

        context.request().bodyHandler(h ->
        {
            JsonObject jsonObject = h.toJsonObject();

            jsonObject.put(ParamConfig.getProjectIdParam(), projectID);

            DeliveryOptions options = new DeliveryOptions().addHeader(ParamConfig.getActionKeyword(), PortfolioDbQuery.getStarProject());

            vertx.eventBus().request(PortfolioDbQuery.getQueue(), jsonObject, options, reply ->
            {
                if(reply.succeeded())
                {
                    JsonObject response = (JsonObject) reply.result().body();

                    HTTPResponseHandler.configureOK(context, response);
                }
            });
        });
    }

    /**
     * Create new project
     * PUT http://localhost:{port}/v2/:annotation_type/newproject/:project_name
     *
     * Example:
     * PUT http://localhost:{port}/v2/bndbox/newproject/helloworld
     *
     */
    public void createProject(RoutingContext context)
    {
        AnnotationType type = AnnotationHandler.getTypeFromEndpoint(context.request().getParam(ParamConfig.getAnnotationTypeParam()));

        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());

        projectFolderSelector.run(projectName, type);

        HTTPResponseHandler.configureOK(context);
    }

    /**
     * Create new project
     * PUT http://localhost:{port}/v2/:annotation_type/rename/:project_name/:new_project_name
     *
     * Example:
     * PUT http://localhost:{port}/v2/bndbox/rename/helloworld/helloworldnewname
     *
     */
    public void renameProject(RoutingContext context)
    {
        AnnotationType type = AnnotationHandler.getTypeFromEndpoint(context.request().getParam(ParamConfig.getAnnotationTypeParam()));

        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());
        String newProjectName = context.request().getParam(ParamConfig.getNewProjectNameParam());

        ProjectLoader loader = ProjectHandler.getProjectLoader(projectName, type);

        if(util.checkIfProjectNull(context, loader, projectName)) return;

        if(ProjectHandler.checkValidProjectRename(newProjectName, type.ordinal()))
        {
            JsonObject jsonObject = new JsonObject();
            jsonObject.put(ParamConfig.getProjectIdParam(), loader.getProjectId());
            jsonObject.put(ParamConfig.getNewProjectNameParam(), newProjectName);

            DeliveryOptions renameOps = new DeliveryOptions().addHeader(ParamConfig.getActionKeyword(), PortfolioDbQuery.getRenameProject());

            loader.setProjectName(newProjectName);

            vertx.eventBus().request(PortfolioDbQuery.getQueue(), jsonObject, renameOps, fetch ->
            {
                JsonObject response = (JsonObject) fetch.result().body();

                if (ReplyHandler.isReplyOk(response))
                {
                    // Update loader in cache after success db update
                    ProjectHandler.updateProjectNameInCache(loader.getProjectId(), loader);
                    log.debug("Rename to " + newProjectName + " success.");
                    HTTPResponseHandler.configureOK(context);
                }
                else
                {
                    HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError("Failed to rename project " + projectName));
                }
            });
        }
        else
        {
            HTTPResponseHandler.configureOK(context);
        }
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
        AnnotationType type = AnnotationHandler.getTypeFromEndpoint(context.request().getParam(ParamConfig.getAnnotationTypeParam()));

        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());

        log.info("Reloading project: " + projectName + " of annotation type: " + type.name());

        ProjectLoader loader = ProjectHandler.getProjectLoader(projectName, type);

        if(util.checkIfProjectNull(context, loader, projectName)) return;

        loader.setFileSystemStatus(FileSystemStatus.WINDOW_CLOSE_LOADING_FILES);

        JsonObject jsonObject = new JsonObject().put(ParamConfig.getProjectIdParam(), loader.getProjectId());

        DeliveryOptions reloadOps = new DeliveryOptions().addHeader(ParamConfig.getActionKeyword(), PortfolioDbQuery.getReloadProject());

        vertx.eventBus().request(PortfolioDbQuery.getQueue(), jsonObject, reloadOps, fetch ->
        {
            JsonObject response = (JsonObject) fetch.result().body();

            if (ReplyHandler.isReplyOk(response))
            {
                HTTPResponseHandler.configureOK(context);

            } else
            {
                HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError("Failed to reload project " + projectName));
            }
        });
    }

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
        AnnotationType type = AnnotationHandler.getTypeFromEndpoint(context.request().getParam(ParamConfig.getAnnotationTypeParam()));

        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());

        ProjectLoader loader = ProjectHandler.getProjectLoader(projectName, type);

        if(util.checkIfProjectNull(context, loader, projectName)) return;

        FileSystemStatus fileSysStatus = loader.getFileSystemStatus();

        JsonObject res = new JsonObject().put(ReplyHandler.getMessageKey(), fileSysStatus.ordinal());

        if(fileSysStatus.equals(FileSystemStatus.WINDOW_CLOSE_DATABASE_UPDATING))
        {
            res.put(ParamConfig.getProgressMetadata(), loader.getProgressUpdate());
        }
        else if(fileSysStatus.equals(FileSystemStatus.WINDOW_CLOSE_DATABASE_UPDATED))
        {
            res.put(ParamConfig.getUuidAdditionListParam(), loader.getReloadAdditionList());
            res.put(ParamConfig.getUuidDeletionListParam(), loader.getReloadDeletionList());
        }
        else if(fileSysStatus.equals(FileSystemStatus.DID_NOT_INITIATE))
        {
            res.put(ReplyHandler.getErrorCodeKey(), ErrorCodes.USER_DEFINED_ERROR.ordinal());
            res.put(ReplyHandler.getErrorMesageKey(), "File / folder selection for project: " + projectName + " did not initiated");
        }

        HTTPResponseHandler.configureOK(context, res);
    }

    /***
     * export a project to configuration file
     *
     * PUT http://localhost:{port}/v2/:annotation_type/projects/:project_name/export/:export_type
     */
    public void exportProject(RoutingContext context)
    {
        AnnotationType type = AnnotationHandler.getTypeFromEndpoint(context.request().getParam(ParamConfig.getAnnotationTypeParam()));

        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());
        String projectId = ProjectHandler.getProjectId(projectName, type.ordinal());

        if(util.checkIfProjectNull(context, projectId, projectName)) return;

        JsonObject request = new JsonObject()
                .put(ParamConfig.getProjectIdParam(), projectId)
                .put(ParamConfig.getAnnotationTypeParam(), type.ordinal())
                .put(ActionConfig.getExportTypeParam(), 2);

        DeliveryOptions options = new DeliveryOptions().addHeader(ParamConfig.getActionKeyword(), PortfolioDbQuery.getExportProject());

        vertx.eventBus().request(PortfolioDbQuery.getQueue(), request, options, reply -> {

            if (reply.succeeded()) {

                JsonObject response = (JsonObject) reply.result().body();

                HTTPResponseHandler.configureOK(context, response);
            }
            else
            {
                HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError("Export of project failed for " + projectName));

            }
        });

    }

    public void importProject(RoutingContext context)
    {
        projectImporter.run();

        HTTPResponseHandler.configureOK(context);
    }

    /**
     * Get file system (file/folder) status for a specific project
     * GET http://localhost:{port}/v2/:annotation_type/projects/:project_name/filesysstatus
     *
     * Example:
     * GET http://localhost:{port}/v2/bndbox/projects/helloworld/filesysstatus
     *
     */
    public void getFileSystemStatus(RoutingContext context)
    {
        AnnotationType type = AnnotationHandler.getTypeFromEndpoint(context.request().getParam(ParamConfig.getAnnotationTypeParam()));

        util.checkIfDockerEnv(context);

        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());

        ProjectLoader loader = ProjectHandler.getProjectLoader(projectName, type);

        if(util.checkIfProjectNull(context, loader, projectName)) return;

        FileSystemStatus fileSysStatus = loader.getFileSystemStatus();

        JsonObject res = new JsonObject().put(ReplyHandler.getMessageKey(), fileSysStatus.ordinal());

        if(fileSysStatus.equals(FileSystemStatus.WINDOW_CLOSE_DATABASE_UPDATING))
        {
            res.put(ParamConfig.getProgressMetadata(), loader.getProgressUpdate());
        }
        else if(fileSysStatus.equals(FileSystemStatus.WINDOW_CLOSE_DATABASE_UPDATED))
        {
            List<String> newAddedUUIDList = loader.getFileSysNewUuidList();

            res.put(ParamConfig.getUuidListParam(), newAddedUUIDList);
        }
        else if(fileSysStatus.equals(FileSystemStatus.WINDOW_CLOSE_DATABASE_NOT_UPDATED))
        {
            // Delete project if user attempt to create project but no path chosen
            ProjectHandler.deleteProjectFromCache(loader.getProjectId());
        }
        else if(fileSysStatus.equals(FileSystemStatus.DID_NOT_INITIATE))
        {
            res.put(ReplyHandler.getErrorCodeKey(), ErrorCodes.USER_DEFINED_ERROR.ordinal());
            res.put(ReplyHandler.getErrorMesageKey(), "File / folder selection for project: " + projectName + " did not initiated");
        }

        HTTPResponseHandler.configureOK(context, res);
    }

    /**
     * Get import project status
     * GET http://localhost:{port}/v2/:annotation_type/projects/importstatus
     *
     * Example:
     * GET http://localhost:{port}/v2/bndbox/projects/importstatus
     *
     */
    public void getImportStatus(RoutingContext context)
    {
        util.checkIfDockerEnv(context);

        FileSystemStatus currentStatus = ProjectImportSelector.getImportFileSystemStatus();

        HTTPResponseHandler.configureOK(context,
                new JsonObject().put(ReplyHandler.getMessageKey(), currentStatus.ordinal()));
    }
}
