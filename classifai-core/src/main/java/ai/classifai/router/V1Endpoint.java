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

import ai.classifai.database.annotation.AnnotationQuery;
import ai.classifai.database.portfolio.PortfolioDbQuery;
import ai.classifai.loader.LoaderStatus;
import ai.classifai.loader.ProjectLoader;
import ai.classifai.selector.annotation.ToolFileSelector;
import ai.classifai.selector.annotation.ToolFolderSelector;
import ai.classifai.selector.filesystem.FileSystemStatus;
import ai.classifai.util.ParamConfig;
import ai.classifai.util.ProjectHandler;
import ai.classifai.util.http.HTTPResponseHandler;
import ai.classifai.util.message.ErrorCodes;
import ai.classifai.util.message.ReplyHandler;
import ai.classifai.util.type.AnnotationHandler;
import ai.classifai.util.type.AnnotationType;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;

/**
 * Classifai v1 endpoints
 *
 * @author devenyantis
 */
@Slf4j
public class V1Endpoint {

    @Setter private Vertx vertx = null;
    @Setter private ToolFileSelector fileSelector = null;
    @Setter private ToolFolderSelector folderSelector = null;

    Util util = new Util();

    /**
     * Get a list of all projects
     * PUT http://localhost:{port}/:annotation_type/projects
     *
     */
    public void getAllProjects(RoutingContext context)
    {
        AnnotationType type = AnnotationHandler.getTypeFromEndpoint(context.request().getParam(ParamConfig.getAnnotationTypeParam()));
        JsonObject request = new JsonObject()
                .put(ParamConfig.getAnnotationTypeParam(), type.ordinal());

        DeliveryOptions options = new DeliveryOptions().addHeader(ParamConfig.getActionKeyword(), PortfolioDbQuery.getRetrieveAllProjectsForAnnotationType());

        vertx.eventBus().request(PortfolioDbQuery.getQueue(), request, options, reply -> {

            if(reply.succeeded())
            {
                JsonObject response = (JsonObject) reply.result().body();

                HTTPResponseHandler.configureOK(context, response);
            }
            else
            {
                HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError("Failure in getting all the projects for " + type.name()));
            }
        });
    }

    /**
     * Retrieve specific project metadata
     *
     * GET http://localhost:{port}/:annotation_type/projects/:project_name/meta
     *
     */
    public void getProjectMetadata(RoutingContext context)
    {
        AnnotationType type = AnnotationHandler.getTypeFromEndpoint(context.request().getParam(ParamConfig.getAnnotationTypeParam()));

        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());

        log.debug("Get metadata of project: " + projectName + " of annotation type: " + type.name());

        ProjectLoader loader = ProjectHandler.getProjectLoader(projectName, type);

        if(util.checkIfProjectNull(context, loader, projectName)) return;

        if(loader == null)
        {
            HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError("Failure in retrieving metadata of project: " + projectName));
        }

        JsonObject jsonObject = new JsonObject().put(ParamConfig.getProjectIdParam(), Objects.requireNonNull(loader).getProjectID());

        //load label list
        DeliveryOptions metadataOptions = new DeliveryOptions().addHeader(ParamConfig.getActionKeyword(), PortfolioDbQuery.getRetrieveProjectMetadata());

        vertx.eventBus().request(PortfolioDbQuery.getQueue(), jsonObject, metadataOptions, metaReply ->
        {
            if (metaReply.succeeded()) {

                JsonObject metaResponse = (JsonObject) metaReply.result().body();

                if (ReplyHandler.isReplyOk(metaResponse))
                {
                    HTTPResponseHandler.configureOK(context, metaResponse);
                }
                else
                {
                    HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError("Failed to retrieve metadata for project " + projectName));
                }
            }
            else
            {
                HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError("Server reply failure message when retrieving project metadata " + projectName));
            }
        });
    }

    /**
     * Get metadata of all projects
     * GET http://localhost:{port}/:annotation_type/projects/meta
     *
     */
    public void getAllProjectsMeta(RoutingContext context)
    {
        AnnotationType type = AnnotationHandler.getTypeFromEndpoint(context.request().getParam(ParamConfig.getAnnotationTypeParam()));

        JsonObject request = new JsonObject()
                .put(ParamConfig.getAnnotationTypeParam(), type.ordinal());

        DeliveryOptions options = new DeliveryOptions().addHeader(ParamConfig.getActionKeyword(), PortfolioDbQuery.getRetrieveAllProjectsMetadata());

        vertx.eventBus().request(PortfolioDbQuery.getQueue(), request, options, reply -> {

            if(reply.succeeded())
            {
                JsonObject response = (JsonObject) reply.result().body();

                HTTPResponseHandler.configureOK(context, response);
            }
            else
            {
                HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError("Failure in getting all the projects for " + type.name()));
            }
        });
    }

    /**
     * Create new project
     * PUT http://localhost:{port}/:annotation_type/newproject/:project_name
     *
     * Example:
     * PUT http://localhost:{port}/seg/newproject/helloworld
     *
     */
    public void createV1NewProject(RoutingContext context)
    {
        AnnotationType type = AnnotationHandler.getTypeFromEndpoint(context.request().getParam(ParamConfig.getAnnotationTypeParam()));

        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());
        JsonObject request = new JsonObject()
                .put(ParamConfig.getProjectNameParam(), projectName)
                .put(ParamConfig.getAnnotationTypeParam(), type.ordinal());

        context.request().bodyHandler(h -> {

            DeliveryOptions createOptions = new DeliveryOptions().addHeader(ParamConfig.getActionKeyword(), PortfolioDbQuery.getCreateNewProject());
            vertx.eventBus().request(PortfolioDbQuery.getQueue(), request, createOptions, reply -> {

                if(reply.succeeded())
                {
                    JsonObject response = (JsonObject) reply.result().body();

                    if(!ReplyHandler.isReplyOk(response))
                    {
                        log.info("Failure in creating new " + type.name() +  " project of name: " + projectName);
                    }
                    HTTPResponseHandler.configureOK(context, response);
                }
            });
        });
    }

    /**
     * Load existing project from the bounding box database
     *
     * GET http://localhost:{port}/:annotation_type/projects/:project_name
     *
     * Example:
     * GET http://localhost:{port}/bndbox/projects/helloworld
     *
     */
    public void loadProject(RoutingContext context)
    {
        AnnotationType type = AnnotationHandler.getTypeFromEndpoint(context.request().getParam(ParamConfig.getAnnotationTypeParam()));

        String queue = util.getDbQuery(type);

        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());

        log.info("Load project: " + projectName + " of annotation type: " + type.name());

        ProjectLoader loader = ProjectHandler.getProjectLoader(projectName, type);

        if(util.checkIfProjectNull(context, loader, projectName)) return;

        loader.toggleFrontEndLoaderParam(); //if project is_new = true, change to false since loading the project

        LoaderStatus loaderStatus = loader.getLoaderStatus();

        //Project exist, did not load in ProjectLoader, proceed with loading and checking validity of uuid from database
        if(loaderStatus.equals(LoaderStatus.DID_NOT_INITIATED) || loaderStatus.equals(LoaderStatus.LOADED))
        {
            loader.setLoaderStatus(LoaderStatus.LOADING);

            JsonObject jsonObject = new JsonObject().put(ParamConfig.getProjectIdParam(), loader.getProjectID());

            DeliveryOptions uuidListOptions = new DeliveryOptions().addHeader(ParamConfig.getActionKeyword(), AnnotationQuery.getLoadValidProjectUuid());

            //start checking uuid if it's path is still exist
            vertx.eventBus().request(queue, jsonObject, uuidListOptions, fetch ->
            {
                JsonObject removalResponse = (JsonObject) fetch.result().body();

                if (ReplyHandler.isReplyOk(removalResponse))
                {
                    HTTPResponseHandler.configureOK(context);

                } else
                {
                    HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError("Failed to load project " + projectName + ". Check validity of data points failed."));
                }
            });

        }
        else if(loaderStatus.equals(LoaderStatus.LOADING))
        {
            HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError("Loading project is in progress in the backend. Did not reinitiated."));
        }
        else if(loaderStatus.equals(LoaderStatus.ERROR))
        {
            HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError("LoaderStatus with error message when loading project " + projectName + ".Loading project aborted. "));
        }
    }


    /**
     * Get status of loading a project
     *
     * GET http://localhost:{port}/:annotation_type/projects/:project_name/loadingstatus
     *
     * Example:
     * GET http://localhost:{port}/seg/projects/helloworld/loadingstatus
     */
    public void loadProjectStatus(RoutingContext context)
    {
        AnnotationType type = AnnotationHandler.getTypeFromEndpoint(context.request().getParam(ParamConfig.getAnnotationTypeParam()));

        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());

        ProjectLoader projectLoader = ProjectHandler.getProjectLoader(projectName, type);

        if (util.checkIfProjectNull(context, projectLoader, projectName)) return;

        LoaderStatus loaderStatus = projectLoader.getLoaderStatus();

        if (loaderStatus.equals(LoaderStatus.LOADING))
        {
            JsonObject jsonObject = new JsonObject();
            jsonObject.put(ReplyHandler.getMessageKey(), loaderStatus.ordinal());

            jsonObject.put(ParamConfig.getProgressMetadata(), projectLoader.getProgress());

            HTTPResponseHandler.configureOK(context, jsonObject);

        } else if (loaderStatus.equals(LoaderStatus.LOADED)) {

            JsonObject jsonObject = new JsonObject();
            jsonObject.put(ReplyHandler.getMessageKey(), loaderStatus.ordinal());

            jsonObject.put(ParamConfig.getLabelListParam(), projectLoader.getLabelList());
            jsonObject.put(ParamConfig.getUuidListParam(), projectLoader.getSanityUuidList());

            HTTPResponseHandler.configureOK(context, jsonObject);

        } else if (loaderStatus.equals(LoaderStatus.DID_NOT_INITIATED) || loaderStatus.equals(LoaderStatus.ERROR))
        {
            JsonObject jsonObject = new JsonObject();
            jsonObject.put(ReplyHandler.getMessageKey(), LoaderStatus.ERROR.ordinal());
            jsonObject.put(ReplyHandler.getErrorMesageKey(), "Loading failed. LoaderStatus error for project " + projectName);

            HTTPResponseHandler.configureOK(context, jsonObject);
        }
    }

    /**
     * Open file system (file/folder) for a specific segmentation project
     *
     * GET http://localhost:{port}/:annotation_type/projects/:project_name/filesys/:file_sys
     *
     * Example:
     * GET http://localhost:{port}/seg/projects/helloworld/filesys/file
     * GET http://localhost:{port}/seg/projects/helloworld/filesys/folder
     */
    public void selectFileSystemType(RoutingContext context)
    {
        AnnotationType type = AnnotationHandler.getTypeFromEndpoint(context.request().getParam(ParamConfig.getAnnotationTypeParam()));

        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());

        if(ParamConfig.isDockerEnv()) log.info("Docker Mode. Choosing file/folder not supported. Use --volume to attach data folder.");
        util.checkIfDockerEnv(context);


        ProjectLoader loader = ProjectHandler.getProjectLoader(projectName, type);

        if(util.checkIfProjectNull(context, loader, projectName)) return;

        FileSystemStatus fileSystemStatus = loader.getFileSystemStatus();

        if(fileSystemStatus.equals(FileSystemStatus.WINDOW_OPEN))
        {
            HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError("File system processing. Not allowed to proceed"));
        }
        else
        {
            HTTPResponseHandler.configureOK(context);

            String fileType = context.request().getParam(ParamConfig.getFileSysParam());

            if(!ProjectHandler.initSelector(fileType))
            {
                JsonObject jsonObject = ReplyHandler.reportUserDefinedError("Filetype with parameter " + fileType + " is not recognizable");
                HTTPResponseHandler.configureOK(context, jsonObject);
                return;
            }

            String currentProjectID = loader.getProjectID();

            if (fileType.equals(ParamConfig.getFileParam()))
            {
                fileSelector.run(currentProjectID);
            }
            else if (fileType.equals(ParamConfig.getFolderParam()))
            {
                folderSelector.run(currentProjectID);
            }
            HTTPResponseHandler.configureOK(context);
        }
    }

    /**
     * Get file system (file/folder) status for a specific project
     * GET http://localhost:{port}/:annotation_type/projects/:project_name/filesysstatus
     *
     * Example:
     * GET http://localhost:{port}/bndbox/projects/helloworld/filesysstatus
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
        else if(fileSysStatus.equals(FileSystemStatus.WINDOW_CLOSE_DATABASE_UPDATED) || (fileSysStatus.equals(FileSystemStatus.WINDOW_CLOSE_DATABASE_NOT_UPDATED)))
        {
            List<String> newAddedUUIDList = loader.getFileSysNewUUIDList();

            res.put(ParamConfig.getUuidListParam(), newAddedUUIDList);

        }
        else if(fileSysStatus.equals(FileSystemStatus.DID_NOT_INITIATE))
        {
            res.put(ReplyHandler.getErrorCodeKey(), ErrorCodes.USER_DEFINED_ERROR.ordinal());
            res.put(ReplyHandler.getErrorMesageKey(), "File / folder selection for project: " + projectName + " did not initiated");
        }

        HTTPResponseHandler.configureOK(context, res);
    }

    /**
     * Retrieve thumbnail with metadata
     *
     * GET http://localhost:{port}/:annotation_type/projects/:project_name/uuid/:uuid/thumbnail
     *
     */
    public void getThumbnail(RoutingContext context)
    {
        AnnotationType type = AnnotationHandler.getTypeFromEndpoint(context.request().getParam(ParamConfig.getAnnotationTypeParam()));

        String queue = util.getDbQuery(type);

        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());
        String projectID = ProjectHandler.getProjectID(projectName, type.ordinal());
        String uuid = context.request().getParam(ParamConfig.getUuidParam());

        JsonObject request = new JsonObject().put(ParamConfig.getUuidParam(), uuid)
                .put(ParamConfig.getProjectIdParam(), projectID)
                .put(ParamConfig.getProjectNameParam(), projectName);

        DeliveryOptions thumbnailOptions = new DeliveryOptions().addHeader(ParamConfig.getActionKeyword(), AnnotationQuery.getQueryData());

        vertx.eventBus().request(queue, request, thumbnailOptions, fetch -> {

            if (fetch.succeeded())
            {
                JsonObject result = (JsonObject) fetch.result().body();

                HTTPResponseHandler.configureOK(context, result);

            }
            else
            {
                HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError("Failure in retrieving thumbnail: " + fetch.cause().getMessage()));
            }
        });
    }

    /***
     *
     * Get Image Source
     *
     * GET http://localhost:{port}/:annotation_type/projects/:project_name/uuid/:uuid/imgsrc
     *
     */
    public void getImageSource(RoutingContext context)
    {
        AnnotationType type = AnnotationHandler.getTypeFromEndpoint(context.request().getParam(ParamConfig.getAnnotationTypeParam()));

        String queue = util.getDbQuery(type);

        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());
        String projectID = ProjectHandler.getProjectID(projectName, type.ordinal());
        String uuid = context.request().getParam(ParamConfig.getUuidParam());

        JsonObject request = new JsonObject()
                .put(ParamConfig.getUuidParam(), uuid)
                .put(ParamConfig.getProjectIdParam(), projectID)
                .put(ParamConfig.getProjectNameParam(), projectName);

        DeliveryOptions imgSrcOptions = new DeliveryOptions().addHeader(ParamConfig.getActionKeyword(), AnnotationQuery.getRetrieveDataPath());

        vertx.eventBus().request(queue, request, imgSrcOptions, fetch -> {

            if (fetch.succeeded()) {

                JsonObject result = (JsonObject) fetch.result().body();

                HTTPResponseHandler.configureOK(context, result);

            }
            else {
                HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError("Failure in getting image source."));
            }
        });
    }

    /***
     *
     * Update labelling information
     *
     * PUT http://localhost:{port}/:annotation_type/projects/:project_name/uuid/:uuid/update
     *
     */
    public void updateData(RoutingContext context)
    {
        AnnotationType type = AnnotationHandler.getTypeFromEndpoint(context.request().getParam(ParamConfig.getAnnotationTypeParam()));
        String queue = util.getDbQuery(type);

        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());

        String projectID = ProjectHandler.getProjectID(projectName, type.ordinal());

        if(util.checkIfProjectNull(context, projectID, projectName)) return;

        context.request().bodyHandler(h ->
        {
            DeliveryOptions updateOptions = new DeliveryOptions().addHeader(ParamConfig.getActionKeyword(), AnnotationQuery.getUpdateData());

            try
            {
                JsonObject jsonObject = h.toJsonObject();
                jsonObject.put(ParamConfig.getProjectIdParam(), projectID);

                vertx.eventBus().request(queue, jsonObject, updateOptions, fetch ->
                {
                    if (fetch.succeeded()) {
                        JsonObject response = (JsonObject) fetch.result().body();

                        HTTPResponseHandler.configureOK(context, response);

                    } else {
                        HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError("Failure in updating database for " + type + " project: " + projectName));
                    }
                });

            }catch (Exception e)
            {
                HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError("Request payload failed to parse: " + projectName + ". " + e));
            }
        });
    }


    /***
     *
     * Update labels
     *
     * PUT http://localhost:{port}/:annotation_type/projects/:project_name/newlabels
     *
     */
    public void updateLabels(RoutingContext context)
    {
        AnnotationType type = AnnotationHandler.getTypeFromEndpoint(context.request().getParam(ParamConfig.getAnnotationTypeParam()));

        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());

        String projectID = ProjectHandler.getProjectID(projectName, type.ordinal());

        if(util.checkIfProjectNull(context, projectID, projectName)) return;

        context.request().bodyHandler(h ->
        {
            try
            {
                JsonObject jsonObject = h.toJsonObject();

                jsonObject.put(ParamConfig.getProjectIdParam(), projectID);

                DeliveryOptions options = new DeliveryOptions().addHeader(ParamConfig.getActionKeyword(), PortfolioDbQuery.getUpdateLabelList());

                vertx.eventBus().request(PortfolioDbQuery.getQueue(), jsonObject, options, reply ->
                {
                    if (reply.succeeded()) {
                        JsonObject response = (JsonObject) reply.result().body();

                        HTTPResponseHandler.configureOK(context, response);
                    }
                });
            }catch (Exception e)
            {
                HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError("Request payload failed to parse: " + projectName + ". " + e));

            }
        });
    }

    /**
     * Delete project
     *
     * DELETE http://localhost:{port}/:annotation_type/projects/:project_name
     *
     * Example:
     * DELETE http://localhost:{port}/bndbox/projects/helloworld
     *
     */
    public void deleteProject(RoutingContext context)
    {
        AnnotationType type = AnnotationHandler.getTypeFromEndpoint(context.request().getParam(ParamConfig.getAnnotationTypeParam()));

        String queue = util.getDbQuery(type);

        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());

        String projectID = ProjectHandler.getProjectID(projectName, type.ordinal());

        if(util.checkIfProjectNull(context, projectID, projectName)) return;

        JsonObject request = new JsonObject()
                .put(ParamConfig.getProjectIdParam(), projectID);

        DeliveryOptions options = new DeliveryOptions().addHeader(ParamConfig.getActionKeyword(), PortfolioDbQuery.getDeleteProject());

        String errorMessage = "Failure in delete project name: " + projectName + " for " + type.name();

        vertx.eventBus().request(PortfolioDbQuery.getQueue(), request, options, reply -> {

            if(reply.succeeded())
            {
                JsonObject response = (JsonObject) reply.result().body();

                if(ReplyHandler.isReplyOk(response)) {
                    //delete in respective Table
                    DeliveryOptions deleteListOptions = new DeliveryOptions().addHeader(ParamConfig.getActionKeyword(), AnnotationQuery.getDeleteProject());

                    vertx.eventBus().request(queue, request, deleteListOptions, fetch -> {

                        if (fetch.succeeded()) {
                            JsonObject replyResponse = (JsonObject) fetch.result().body();

                            //delete in Project Handler
                            ProjectHandler.deleteProjectWithID(projectID);
                            HTTPResponseHandler.configureOK(context, replyResponse);
                        } else {
                            HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError(errorMessage));
                        }
                    });
                }
            }
            else
            {
                HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError(errorMessage  + " from Portfolio Database"));
            }
        });
    }

    /**
     * Delete uuid of a specific project
     *
     * DELETE http://localhost:{port}/:annotation_type/projects/:project_name/uuids
     *
     * Example:
     * DELETE http://localhost:{port}/bndbox/projects/helloworld/uuids
     *
     */
    public void deleteProjectUUID(RoutingContext context)
    {
        AnnotationType type = AnnotationHandler.getTypeFromEndpoint(context.request().getParam(ParamConfig.getAnnotationTypeParam()));

        String queue = util.getDbQuery(type);

        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());

        String projectID = ProjectHandler.getProjectID(projectName, type.ordinal());

        if(util.checkIfProjectNull(context, projectID, projectName)) return;

        context.request().bodyHandler(h ->
        {
            JsonObject request = h.toJsonObject();

            JsonArray uuidListArray = request.getJsonArray(ParamConfig.getUuidListParam());

            request.put(ParamConfig.getProjectIdParam(), projectID).put(ParamConfig.getUuidListParam(), uuidListArray);

            DeliveryOptions options = new DeliveryOptions().addHeader(ParamConfig.getActionKeyword(), AnnotationQuery.getDeleteSelectionUuidList());

            vertx.eventBus().request(queue, request, options, reply ->
            {
                if (reply.succeeded())
                {
                    JsonObject response = (JsonObject) reply.result().body();

                    HTTPResponseHandler.configureOK(context, response);
                }
            });
        });
    }
}
