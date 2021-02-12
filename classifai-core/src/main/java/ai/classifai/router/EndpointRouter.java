/*
 * Copyright (c) 2020-2021 CertifAI Sdn. Bhd.
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

import ai.classifai.database.annotation.bndbox.BoundingBoxDbQuery;
import ai.classifai.database.annotation.seg.SegDbQuery;
import ai.classifai.database.portfolio.PortfolioDbQuery;
import ai.classifai.loader.LoaderStatus;
import ai.classifai.loader.ProjectLoader;
import ai.classifai.selector.annotation.ToolFileSelector;
import ai.classifai.selector.annotation.ToolFolderSelector;
import ai.classifai.selector.filesystem.FileSystemStatus;
import ai.classifai.selector.project.ProjectFolderSelector;
import ai.classifai.util.ParamConfig;
import ai.classifai.util.ProjectHandler;
import ai.classifai.util.collection.ConversionHandler;
import ai.classifai.util.http.HTTPResponseHandler;
import ai.classifai.util.message.ErrorCodes;
import ai.classifai.util.message.ReplyHandler;
import ai.classifai.util.type.AnnotationType;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Endpoint routing for different url requests
 *
 * @author codenamewei
 */
@Slf4j
public class EndpointRouter extends AbstractVerticle
{
    private ToolFileSelector fileSelector;
    private ToolFolderSelector folderSelector;
    private ProjectFolderSelector  projectFolderSelector;

    public EndpointRouter()
    {
        Thread threadfile = new Thread(){
            public void run(){
                fileSelector = new ToolFileSelector();
            }
        };
        threadfile.start();

        Thread threadfolder = new Thread(){
            public void run(){
                folderSelector = new ToolFolderSelector();
            }
        };
        threadfolder.start();

        Thread projectFolder = new Thread(){
            public void run(){
                projectFolderSelector = new ProjectFolderSelector();
            }
        };
        projectFolder.start();

    }

    /**
     * Get a list of all projects under the category of bounding box
     * PUT http://localhost:{port}/bndbox/projects
     *
     */
    private void getAllBndBoxProjects(RoutingContext context)
    {
        getAllProjects(context, AnnotationType.BOUNDINGBOX);
    }

    /**
     * Get a list of all projects under the category of segmentation
     * PUT http://localhost:{port}/seg/projects
     *
     */
    private void getAllSegProjects(RoutingContext context)
    {
        getAllProjects(context, AnnotationType.SEGMENTATION);
    }

    private void getAllProjects(RoutingContext context, AnnotationType type)
    {
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
     * Retrieve metadata of bounding box project
     *
     * GET http://localhost:{port}/bndbox/projects/:project_name/meta
     *
     */
    public void getBndBoxProjectMeta(RoutingContext context)
    {
        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());

        getProjectMetadata(context, projectName, AnnotationType.BOUNDINGBOX);
    }

    /**
     * Retrieve metadata of segmentation project
     *
     * GET http://localhost:{port}/seg/projects/:project_name/meta
     *
     */
    public void getSegProjectMeta(RoutingContext context)
    {
        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());

        getProjectMetadata(context, projectName, AnnotationType.SEGMENTATION);
    }

    /**
     * Retrieve metadata of project
     */
    private void getProjectMetadata(RoutingContext context, String projectName, AnnotationType annotationType)
    {
        log.debug("Get metadata of project: " + projectName + " of annotation type: " + annotationType.name());

        ProjectLoader loader = ProjectHandler.getProjectLoader(projectName, annotationType);

        if(checkIfProjectNull(context, loader, projectName)) return;

        if(loader == null)
        {
            HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError("Failure in retrieving metadata of project: " + projectName));
        }

        JsonObject jsonObject = new JsonObject().put(ParamConfig.getProjectIdParam(), loader.getProjectID());

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
     * Get metadata of all bounding box projects
     * GET http://localhost:{port}/bndbox/projects/meta
     *
     */
    private void getAllBndBoxProjectsMeta(RoutingContext context)
    {
        JsonObject request = new JsonObject()
                .put(ParamConfig.getAnnotationTypeParam(), AnnotationType.BOUNDINGBOX.ordinal());

        getAllProjectsMeta(context, request, AnnotationType.BOUNDINGBOX);
    }

    /**
     * Get metadata of all segmentation projects
     * GET http://localhost:{port}/seg/projects/meta
     *
     */
    private void getAllSegProjectsMeta(RoutingContext context)
    {
        JsonObject request = new JsonObject()
                .put(ParamConfig.getAnnotationTypeParam(), AnnotationType.SEGMENTATION.ordinal());

        getAllProjectsMeta(context, request, AnnotationType.SEGMENTATION);
    }

    private void getAllProjectsMeta(RoutingContext context, JsonObject request, AnnotationType type)
    {
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
     * Create new project under the category of bounding box
     * PUT http://localhost:{port}/v2/bndbox/newproject/:project_name
     *
     * Example:
     * PUT http://localhost:{port}/v2/bndbox/newproject/helloworld
     *
     */
    private void createV2BndBoxProject(RoutingContext context)
    {
        createV2Project(context, AnnotationType.BOUNDINGBOX);
    }

    private void createV2Project(RoutingContext context, AnnotationType annotationType)
    {
        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());

        projectFolderSelector.run(projectName, annotationType);

        HTTPResponseHandler.configureOK(context);
    }


    /**
     * Create new project under the category of bounding box
     * PUT http://localhost:{port}/bndbox/newproject/:project_name
     *
     * Example:
     * PUT http://localhost:{port}/bndbox/newproject/helloworld
     *
     */
    private void createV1BndBoxProject(RoutingContext context)
    {
        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());
        JsonObject request = new JsonObject()
                .put(ParamConfig.getProjectNameParam(), projectName)
                .put(ParamConfig.getAnnotationTypeParam(), AnnotationType.BOUNDINGBOX.ordinal());

        createV1NewProject(context, request, AnnotationType.BOUNDINGBOX);
    }

    /**
     * Create new project under the category of segmentation
     * PUT http://localhost:{port}/seg/newproject/:project_name
     *
     * Example:
     * PUT http://localhost:{port}/seg/newproject/helloworld
     *
     */
    private void createV1SegProject(RoutingContext context)
    {
        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());
        JsonObject request = new JsonObject()
                .put(ParamConfig.getProjectNameParam(), projectName)
                .put(ParamConfig.getAnnotationTypeParam(), AnnotationType.SEGMENTATION.ordinal());

        createV1NewProject(context, request, AnnotationType.SEGMENTATION);
    }

    private void createV1NewProject(RoutingContext context, JsonObject request, AnnotationType annotationType)
    {
        context.request().bodyHandler(h -> {

            DeliveryOptions createOptions = new DeliveryOptions().addHeader(ParamConfig.getActionKeyword(), PortfolioDbQuery.getCreateNewProject());

            vertx.eventBus().request(PortfolioDbQuery.getQueue(), request, createOptions, reply -> {

                if(reply.succeeded())
                {
                    JsonObject response = (JsonObject) reply.result().body();

                    if(ReplyHandler.isReplyOk(response) == false)
                    {
                        String projectName = request.getString(ParamConfig.getProjectNameParam());

                        log.info("Failure in creating new " + annotationType.name() +  " project of name: " + projectName);
                    }
                    HTTPResponseHandler.configureOK(context, response);
                }
            });
        });
    }

    /**
     * Load existing project from the bounding box database
     *
     * GET http://localhost:{port}/bndbox/projects/:project_name
     *
     * Example:
     * GET http://localhost:{port}/bndbox/projects/helloworld
     *
     */
    private void loadBndBoxProject(RoutingContext context)
    {
        loadProject(context, BoundingBoxDbQuery.getQueue(), BoundingBoxDbQuery.getLoadValidProjectUUID(), AnnotationType.BOUNDINGBOX);
    }

    /**
     * Load existing project from the segmentation database
     *
     * GET http://localhost:{port}/seg/projects/:project_name
     *
     * Example:
     * GET http://localhost:{port}/seg/projects/helloworld
     *
     */
    private void loadSegProject(RoutingContext context)
    {
        loadProject(context, SegDbQuery.getQueue(), SegDbQuery.getLoadValidProjectUUID(), AnnotationType.SEGMENTATION);
    }

    public void loadProject(RoutingContext context, String queue, String query, AnnotationType annotationType)
    {
        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());

        log.info("Load project: " + projectName + " of annotation type: " + annotationType.name());

        ProjectLoader loader = ProjectHandler.getProjectLoader(projectName, annotationType);

        if(checkIfProjectNull(context, loader, projectName)) return;

        loader.toggleFrontEndLoaderParam(); //if project is_new = true, change to false since loading the project

        LoaderStatus loaderStatus = loader.getLoaderStatus();

        //Project exist, did not load in ProjectLoader, proceed with loading and checking validity of uuid from database
        if(loaderStatus.equals(LoaderStatus.DID_NOT_INITIATED) || loaderStatus.equals(LoaderStatus.LOADED))
        {
            loader.setLoaderStatus(LoaderStatus.LOADING);

            JsonObject jsonObject = new JsonObject().put(ParamConfig.getProjectIdParam(), loader.getProjectID());

            DeliveryOptions uuidListOptions = new DeliveryOptions().addHeader(ParamConfig.getActionKeyword(), query);

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
     * GET http://localhost:{port}/bndbox/projects/:project_name/loadingstatus
     *
     * Example:
     * GET http://localhost:{port}/bndbox/projects/helloworld/loadingstatus
     */
    private void loadBndBoxProjectStatus(RoutingContext context)
    {
        loadProjectStatus(context, AnnotationType.BOUNDINGBOX);
    }

    /**
     * Get status of loading a project
     *
     * GET http://localhost:{port}/seg/projects/:project_name/loadingstatus
     *
     * Example:
     * GET http://localhost:{port}/seg/projects/helloworld/loadingstatus
     */
    private void loadSegProjectStatus(RoutingContext context)
    {
        loadProjectStatus(context, AnnotationType.SEGMENTATION);
    }

    /**
     * Get status of loading a project
     *
     * GET http://localhost:{port}/bndbox/projects/:project_name/loadingstatus
     * GET http://localhost:{port}/seg/projects/:project_name/loadingstatus
     *
     * Example:
     * GET http://localhost:{port}/seg/projects/helloworld/loadingstatus
     */
    private void loadProjectStatus(RoutingContext context, AnnotationType annotationType)
    {
        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());

        ProjectLoader projectLoader = ProjectHandler.getProjectLoader(projectName, annotationType);

        if (checkIfProjectNull(context, projectLoader, projectName)) return;

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
            jsonObject.put(ParamConfig.getUuidListParam(), projectLoader.getSanityUUIDList());

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
     * Open file system (file/folder) for a specific bounding box project
     *
     * GET http://localhost:{port}/bndbox/projects/:project_name/filesys/:file_sys
     *
     * Example:
     * GET http://localhost:{port}/bndbox/projects/helloworld/filesys/file
     * GET http://localhost:{port}/bndbox/projects/helloworld/filesys/folder
     */
    private void selectBndBoxFileSystemType(RoutingContext context)
    {
        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());

        selectFileSystemType(context, projectName, AnnotationType.BOUNDINGBOX);
    }

    /**
     * Open file system (file/folder) for a specific segmentation project
     *
     * GET http://localhost:{port}/seg/projects/:project_name/filesys/:file_sys
     *
     * Example:
     * GET http://localhost:{port}/seg/projects/helloworld/filesys/file
     * GET http://localhost:{port}/seg/projects/helloworld/filesys/folder
     */
    private void selectSegFileSystemType(RoutingContext context)
    {
        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());

        selectFileSystemType(context, projectName, AnnotationType.SEGMENTATION);
    }

    private void selectFileSystemType(RoutingContext context, String projectName, AnnotationType annotationType)
    {
        if(ParamConfig.isDockerEnv()) log.info("Docker Mode. Choosing file/folder not supported. Use --volume to attach data folder.");
        checkIfDockerEnv(context);


        ProjectLoader loader = ProjectHandler.getProjectLoader(projectName, annotationType);

        if(checkIfProjectNull(context, loader, projectName)) return;

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
     * Get file system (file/folder) status for a specific boundingbox project
     *
     * GET http://localhost:{port}/bndbox/projects/:project_name/filesysstatus
     *
     * Example:
     * GET http://localhost:{port}/bndbox/projects/helloworld/filesysstatus
     *
     */
    private void getBndBoxFileSystemStatus(RoutingContext context)
    {
        getFileSystemStatus(context, AnnotationType.BOUNDINGBOX);
    }

    /**
     * Get file system (file/folder) status for a specific segmentation project
     *
     * GET http://localhost:{port}/seg/projects/:project_name/filesysstatus
     *
     * Example:
     * GET http://localhost:{port}/seg/projects/helloworld/filesysstatus
     *
     */
    private void getSegFileSystemStatus(RoutingContext context)
    {
        getFileSystemStatus(context, AnnotationType.SEGMENTATION);
    }

    /**
     * Get file system (file/folder) status for a specific project
     * GET http://localhost:{port}/bndbox/projects/:project_name/filesysstatus
     * GET http://localhost:{port}/seg/projects/:project_name/filesysstatus
     *
     * Example:
     * GET http://localhost:{port}/bndbox/projects/helloworld/filesysstatus
     *
     */
    private void getFileSystemStatus(RoutingContext context, AnnotationType annotationType) {

        checkIfDockerEnv(context);

        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());

        ProjectLoader loader = ProjectHandler.getProjectLoader(projectName, annotationType);

        if(checkIfProjectNull(context, loader, projectName));

        FileSystemStatus fileSysStatus = loader.getFileSystemStatus();

        JsonObject res = new JsonObject().put(ReplyHandler.getMessageKey(), fileSysStatus.ordinal());

        if(fileSysStatus.equals(FileSystemStatus.WINDOW_CLOSE_DATABASE_UPDATING))
        {
            res.put(ParamConfig.getProgressMetadata(), loader.getProgressUpdate());
        }
        else if(fileSysStatus.equals(FileSystemStatus.WINDOW_CLOSE_DATABASE_UPDATED) | (fileSysStatus.equals(FileSystemStatus.WINDOW_CLOSE_DATABASE_NOT_UPDATED)))
        {
            List<String> newAddedUUIDList = loader.getFileSysNewUUIDList();

            res.put(ParamConfig.getUuidListParam(), newAddedUUIDList);

        }
        else if(fileSysStatus.equals(FileSystemStatus.DID_NOT_INITIATE)) {
            res.put(ReplyHandler.getErrorCodeKey(), ErrorCodes.USER_DEFINED_ERROR.ordinal());
            res.put(ReplyHandler.getErrorMesageKey(), "File / folder selection for project: " + projectName + "did not initiated");
        }

        HTTPResponseHandler.configureOK(context, res);
    }

    private void checkIfDockerEnv(RoutingContext context)
    {
        if(ParamConfig.isDockerEnv())
        {
            HTTPResponseHandler.configureOK(context);
        }
    }

    /**
     * Retrieve thumbnail with metadata
     *
     * GET http://localhost:{port}/bndbox/projects/:project_name/uuid/:uuid/thumbnail
     *
     */
    private void getBndBoxThumbnail(RoutingContext context)
    {
        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());
        String projectID = ProjectHandler.getProjectID(projectName, AnnotationType.BOUNDINGBOX.ordinal());
        String uuid = context.request().getParam(ParamConfig.getUuidParam());

        JsonObject request = new JsonObject().put(ParamConfig.getUuidParam(), uuid)
                .put(ParamConfig.getProjectIdParam(), projectID)
                .put(ParamConfig.getProjectNameParam(), projectName);

        getThumbnail(context, BoundingBoxDbQuery.getQueue(), BoundingBoxDbQuery.getRetrieveData(), request);
    }

    /**
     * Retrieve thumbnail with metadata
     *
     * GET http://localhost:{port}/seg/projects/:project_name/uuid/:uuid/thumbnail
     *
     */
    private void getSegThumbnail(RoutingContext context)
    {
        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());
        String projectID = ProjectHandler.getProjectID(projectName, AnnotationType.SEGMENTATION.ordinal());
        String uuid = context.request().getParam(ParamConfig.getUuidParam());

        JsonObject request = new JsonObject().put(ParamConfig.getUuidParam(), uuid)
                .put(ParamConfig.getProjectIdParam(), projectID)
                .put(ParamConfig.getProjectNameParam(), projectName);

        getThumbnail(context, SegDbQuery.getQueue(), SegDbQuery.getRetrieveData(), request);
    }

    private void getThumbnail(RoutingContext context, String queue, String query, JsonObject request)
    {
        DeliveryOptions thumbnailOptions = new DeliveryOptions().addHeader(ParamConfig.getActionKeyword(), query);

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
     * GET http://localhost:{port}/bndbox/projects/:project_name/uuid/:uuid/imgsrc
     *
     */
    private void getBndBoxImageSource(RoutingContext context) {
        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());
        String projectID = ProjectHandler.getProjectID(projectName, AnnotationType.BOUNDINGBOX.ordinal());
        String uuid = context.request().getParam(ParamConfig.getUuidParam());

        JsonObject request = new JsonObject()
                .put(ParamConfig.getUuidParam(), uuid)
                .put(ParamConfig.getProjectIdParam(), projectID)
                .put(ParamConfig.getProjectNameParam(), projectName);

        getImageSource(context, BoundingBoxDbQuery.getQueue(), BoundingBoxDbQuery.getRetrieveDataPath(), request);
    }

    /***
     *
     * Get Image Source
     *
     * GET http://localhost:{port}/seg/projects/:project_name/uuid/:uuid/imgsrc
     *
     */
    private void getSegImageSource(RoutingContext context) {
        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());
        String projectID = ProjectHandler.getProjectID(projectName, AnnotationType.SEGMENTATION.ordinal());
        String uuid = context.request().getParam(ParamConfig.getUuidParam());

        JsonObject request = new JsonObject().put(ParamConfig.getUuidParam(), uuid)
                .put(ParamConfig.getProjectIdParam(), projectID);

        getImageSource(context, SegDbQuery.getQueue(), SegDbQuery.getRetrieveDataPath(), request);
    }

    private void getImageSource(RoutingContext context, String queue, String query, JsonObject request)
    {
        DeliveryOptions imgSrcOptions = new DeliveryOptions().addHeader(ParamConfig.getActionKeyword(), query);

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
     * Update bounding box labelling information
     *
     * PUT http://localhost:{port}/bndbox/projects/:project_name/uuid/:uuid/update
     *
     */
    private void updateBndBoxData(RoutingContext context)
    {
        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());

        String projectID = ProjectHandler.getProjectID(projectName, AnnotationType.BOUNDINGBOX.ordinal());

        if(checkIfProjectNull(context, projectID, projectName)) return;

        context.request().bodyHandler(h ->
        {
            DeliveryOptions updateOptions = new DeliveryOptions().addHeader(ParamConfig.getActionKeyword(), BoundingBoxDbQuery.getUpdateData());

            try
            {
                io.vertx.core.json.JsonObject jsonObject = ConversionHandler.json2JSONObject(h.toJson());
                jsonObject.put(ParamConfig.getProjectIdParam(), projectID);

                vertx.eventBus().request(BoundingBoxDbQuery.getQueue(), jsonObject, updateOptions, fetch ->
                {
                    if (fetch.succeeded()) {
                        JsonObject response = (JsonObject) fetch.result().body();

                        HTTPResponseHandler.configureOK(context, response);

                    } else {
                        HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError("Failure in updating database for bounding box project: " + projectName));
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
     * Update segmentation labelling information
     *
     * PUT http://localhost:{port}/seg/projects/:project_name/uuid/:uuid/update
     *
     */
    private void updateSegData(RoutingContext context) {
        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());

        String projectID = ProjectHandler.getProjectID(projectName, AnnotationType.SEGMENTATION.ordinal());

        if (checkIfProjectNull(context, projectID, projectName)) return;

        context.request().bodyHandler(h ->
        {
            DeliveryOptions updateOptions = new DeliveryOptions().addHeader(ParamConfig.getActionKeyword(), SegDbQuery.getUpdateData());

            try {
                io.vertx.core.json.JsonObject jsonObject = ConversionHandler.json2JSONObject(h.toJson());
                jsonObject.put(ParamConfig.getProjectIdParam(), ProjectHandler.getProjectID(projectName, AnnotationType.SEGMENTATION.ordinal()));

                vertx.eventBus().request(SegDbQuery.getQueue(), jsonObject, updateOptions, fetch ->
                {
                    if (fetch.succeeded()) {
                        JsonObject response = (JsonObject) fetch.result().body();

                        HTTPResponseHandler.configureOK(context, response);

                    } else {
                        HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError("Failure in updating database for segmentation project."));
                    }
                });
            } catch (Exception e) {
                HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError("Request payload failed to parse: " + projectName + ". " + e));
            }
        });
    }

    /***
     *
     * Update labels
     *
     * PUT http://localhost:{port}/bndbox/projects/:project_name/newlabels
     *
     */
    private void updateBndBoxLabels(RoutingContext context)
    {
        updateLabels(context, AnnotationType.BOUNDINGBOX);
    }


    /***
     *
     * Update labels
     *
     * PUT http://localhost:{port}/seg/projects/:project_name/newlabels
     *
     */
    private void updateSegLabels(RoutingContext context)
    {
        updateLabels(context, AnnotationType.SEGMENTATION);
    }


    /***
     *
     * Update labels
     *
     * PUT http://localhost:{port}/bndbox/projects/:project_name/newlabels
     * PUT http://localhost:{port}/seg/projects/:project_name/newlabels
     *
     */
    private void updateLabels(RoutingContext context, AnnotationType annotationType)
    {
        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());

        String projectID = ProjectHandler.getProjectID(projectName, annotationType.ordinal());

        if(checkIfProjectNull(context, projectID, projectName)) return;

        context.request().bodyHandler(h ->
        {
            try
            {
                io.vertx.core.json.JsonObject jsonObject = ConversionHandler.json2JSONObject(h.toJson());

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

    /***
     * change is_load state of a bounding box project to false
     *
     * PUT http://localhost:{port}/bndbox/projects/:project_name
     */
    private void closeBndBoxProjectState(RoutingContext context)
    {
        closeProjectState(context, AnnotationType.BOUNDINGBOX);
    }

    /***
     * change is_load state of a segmentation project to false
     *
     * PUT http://localhost:{port}/seg/projects/:project_name
     */
    private void closeSegProjectState(RoutingContext context)
    {
        closeProjectState(context, AnnotationType.SEGMENTATION);
    }

    private void closeProjectState(RoutingContext context, AnnotationType annotationType)
    {
        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());
        String projectID = ProjectHandler.getProjectID(projectName, annotationType.ordinal());

        if(checkIfProjectNull(context, projectID, projectName)) return;

        context.request().bodyHandler(h ->
        {
            try
            {
                io.vertx.core.json.JsonObject jsonObject = ConversionHandler.json2JSONObject(h.toJson());

                if(jsonObject.getString(ParamConfig.getStatusParam()).equals("closed"))
                {
                    ProjectHandler.getProjectLoader(projectID).setIsLoadedFrontEndToggle(Boolean.FALSE);
                }
                else
                {
                    throw new Exception("Request payload failed to satisfied the status of {\"status\": \"closed\"} for " + projectName + ". ");
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
     * Star a bounding box project
     *
     * PUT http://localhost:{port}/bndbox/projects/:projectname/star
     */
    private void starBndBoxProject(RoutingContext context)
    {
        starProject(context, AnnotationType.BOUNDINGBOX);
    }

    /***
     * Star a segmentation project
     *
     * PUT http://localhost:{port}/seg/projects/:projectname/star
     */
    private void starSegProject(RoutingContext context)
    {
        starProject(context, AnnotationType.SEGMENTATION);
    }

    private void starProject(RoutingContext context, AnnotationType annotationType)
    {
        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());
        String projectID = ProjectHandler.getProjectID(projectName, annotationType.ordinal());

        if(checkIfProjectNull(context, projectID, projectName)) return;

        context.request().bodyHandler(h ->
        {
            io.vertx.core.json.JsonObject jsonObject = ConversionHandler.json2JSONObject(h.toJson());

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

    private boolean checkIfProjectNull(RoutingContext context, Object project, @NonNull String projectName)
    {
        if(project == null)
        {
            HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError("Project not found: " + projectName));

            return true;
        }

        return false;
    }

    /**
     * Delete bounding box project
     *
     * DELETE http://localhost:{port}/bndbox/projects/:project_name
     *
     * Example:
     * DELETE http://localhost:{port}/bndbox/projects/helloworld
     *
     */
    private void deleteBndBoxProject(RoutingContext context)
    {
        //delete in Portfolio Table
        deleteProject(context, BoundingBoxDbQuery.getQueue(), BoundingBoxDbQuery.getDeleteProjectUuidListWithProjectId(), AnnotationType.BOUNDINGBOX);
    }

    /**
     * Delete Segmentation project
     *
     * DELETE http://localhost:{port}/seg/projects/:project_name
     *
     * Example:
     * DELETE http://localhost:{port}/seg/projects/helloworld
     *
     */
    private void deleteSegProject(RoutingContext context)
    {
        deleteProject(context, SegDbQuery.getQueue(), SegDbQuery.getDeleteProjectUuidListWithProjectId(), AnnotationType.SEGMENTATION);
    }

    private void deleteProject(RoutingContext context, String queue, String query,  AnnotationType type)
    {
        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());

        String projectID = ProjectHandler.getProjectID(projectName, type.ordinal());

        if(checkIfProjectNull(context, projectID, projectName)) return;

        JsonObject request = new JsonObject()
                .put(ParamConfig.getProjectIdParam(), projectID);

        DeliveryOptions options = new DeliveryOptions().addHeader(ParamConfig.getActionKeyword(), PortfolioDbQuery.getDeleteProject());

        vertx.eventBus().request(PortfolioDbQuery.getQueue(), request, options, reply -> {

            if(reply.succeeded())
            {
                JsonObject response = (JsonObject) reply.result().body();

                if(ReplyHandler.isReplyOk(response))
                {
                    //delete in respective Table
                    DeliveryOptions deleteListOptions = new DeliveryOptions().addHeader(ParamConfig.getActionKeyword(), query);

                    vertx.eventBus().request(queue, request, deleteListOptions, fetch -> {

                        if (fetch.succeeded())
                        {
                            JsonObject replyResponse = (JsonObject) fetch.result().body();

                            //delete in Project Handler
                            ProjectHandler.deleteProjectWithID(projectID);
                            HTTPResponseHandler.configureOK(context, replyResponse);
                        }
                        else
                        {
                            HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError("Failure in delete project name: " + projectName + " for " + type.name()));
                        }
                    });
                }
                else
                {
                    HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError("Failure in delete project name: " + projectName + " for " + type.name() + " from " + type.name() + " Database"));
                }

            }
            else
            {
                HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError("Failure in delete project name: " + projectName + " for " + type.name()  + " from Portfolio Database"));
            }
        });
    }

    /**
     * Delete uuid of a specific bounding box project
     *
     * DELETE http://localhost:{port}/bndbox/projects/:project_name/uuids
     *
     * Example:
     * DELETE http://localhost:{port}/bndbox/projects/helloworld/uuids
     *
     */
    private void deleteBndBoxProjectUUID(RoutingContext context)
    {
        deleteProjectUUID(context, BoundingBoxDbQuery.getQueue(), BoundingBoxDbQuery.getDeleteProjectUuidList(), AnnotationType.BOUNDINGBOX);
    }

    /**
     * Delete uuid of a specific segmentation project
     *
     * DELETE http://localhost:{port}/seg/projects/:project_name/uuids
     *
     * Example:
     * DELETE http://localhost:{port}/seg/projects/helloworld/uuids
     *
     */
    private void deleteSegProjectUUID(RoutingContext context)
    {
        deleteProjectUUID(context, SegDbQuery.getQueue(), SegDbQuery.getDeleteProjectUuidList(), AnnotationType.SEGMENTATION);
    }

    private void deleteProjectUUID(RoutingContext context, String queue, String query, AnnotationType annotationType)
    {
        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());

        String projectID = ProjectHandler.getProjectID(projectName, annotationType.ordinal());

        if(checkIfProjectNull(context, projectID, projectName)) return;

        context.request().bodyHandler(h ->
        {
            io.vertx.core.json.JsonObject request = ConversionHandler.json2JSONObject(h.toJson());

            JsonArray uuidListArray = request.getJsonArray(ParamConfig.getUuidListParam());

            request.put(ParamConfig.getProjectIdParam(), projectID).put(ParamConfig.getUuidListParam(), uuidListArray);

            DeliveryOptions options = new DeliveryOptions().addHeader(ParamConfig.getActionKeyword(), query);

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

    @Override
    public void stop(Promise<Void> promise) {
        log.debug("Endpoint Router Verticle stopping...");

        //add action before stopped if necessary
    }

    @Override
    public void start(Promise<Void> promise)
    {
        Router router = Router.router(vertx);

        //display for content in webroot
        router.route().handler(StaticHandler.create());

        //*******************************Bounding Box*******************************

        router.get("/bndbox/projects").handler(this::getAllBndBoxProjects);

        router.get("/bndbox/projects/meta").handler(this::getAllBndBoxProjectsMeta);

        router.get("/bndbox/projects/:project_name/meta").handler(this::getBndBoxProjectMeta);

        router.put("/bndbox/newproject/:project_name").handler(this::createV1BndBoxProject);

        router.get("/bndbox/projects/:project_name").handler(this::loadBndBoxProject);

        router.delete("/bndbox/projects/:project_name").handler(this::deleteBndBoxProject);

        router.delete("/bndbox/projects/:project_name/uuids").handler(this::deleteBndBoxProjectUUID);

        router.get("/bndbox/projects/:project_name/loadingstatus").handler(this::loadBndBoxProjectStatus);

        router.get("/bndbox/projects/:project_name/filesys/:file_sys").handler(this::selectBndBoxFileSystemType);

        router.get("/bndbox/projects/:project_name/filesysstatus").handler(this::getBndBoxFileSystemStatus);

        router.get("/bndbox/projects/:project_name/uuid/:uuid/thumbnail").handler(this::getBndBoxThumbnail);

        router.get("/bndbox/projects/:project_name/uuid/:uuid/imgsrc").handler(this::getBndBoxImageSource);

        router.put("/bndbox/projects/:project_name/uuid/:uuid/update").handler(this::updateBndBoxData);

        router.put("/bndbox/projects/:project_name/newlabels").handler(this::updateBndBoxLabels);

        //v2

        router.put("/bndbox/projects/:project_name").handler(this::closeBndBoxProjectState);

        router.put("/bndbox/projects/:project_name/star").handler(this::starBndBoxProject);

        router.put("/v2/bndbox/newproject/:project_name").handler(this::createV2BndBoxProject);

        //*******************************Segmentation*******************************

        router.get("/seg/projects").handler(this::getAllSegProjects);

        router.get("/seg/projects/meta").handler(this::getAllSegProjectsMeta);

        router.get("/seg/projects/:project_name/meta").handler(this::getSegProjectMeta);

        router.put("/seg/newproject/:project_name").handler(this::createV2SegProject);

        router.get("/seg/projects/:project_name").handler(this::loadSegProject);

        router.delete("/seg/projects/:project_name").handler(this::deleteSegProject);

        router.delete("/seg/projects/:project_name/uuids").handler(this::deleteSegProjectUUID);

        router.get("/seg/projects/:project_name/loadingstatus").handler(this::loadSegProjectStatus);

        router.get("/seg/projects/:project_name/filesys/:file_sys").handler(this::selectSegFileSystemType);

        router.get("/seg/projects/:project_name/filesysstatus").handler(this::getSegFileSystemStatus);

        router.get("/seg/projects/:project_name/uuid/:uuid/thumbnail").handler(this::getSegThumbnail);

        router.get("/seg/projects/:project_name/uuid/:uuid/imgsrc").handler(this::getSegImageSource);

        router.put("/seg/projects/:project_name/uuid/:uuid/update").handler(this::updateSegData);

        router.put("/seg/projects/:project_name/newlabels").handler(this::updateSegLabels);

        //v2

        router.put("/seg/projects/:project_name").handler(this::closeSegProjectState);

        router.put("/seg/projects/:project_name/star").handler(this::starSegProject);

        vertx.createHttpServer()
                .requestHandler(router)
                .exceptionHandler(Throwable::printStackTrace)
                .listen(ParamConfig.getHostingPort(), r -> {

                    if (r.succeeded())
                    {
                        promise.complete();
                    }
                    else {
                        log.debug("Failure in creating HTTPServer in ServerVerticle. ", r.cause().getMessage());
                        promise.fail(r.cause());
                    }
                });
    }
}
