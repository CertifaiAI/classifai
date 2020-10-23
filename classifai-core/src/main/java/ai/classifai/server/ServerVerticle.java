/*
 * Copyright (c) 2020 CertifAI Sdn. Bhd.
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
package ai.classifai.server;

import ai.classifai.annotation.AnnotationType;
import ai.classifai.database.boundingboxdb.BoundingBoxDbQuery;
import ai.classifai.database.loader.LoaderStatus;
import ai.classifai.database.loader.ProjectLoader;
import ai.classifai.database.portfoliodb.PortfolioDbQuery;
import ai.classifai.database.segdb.SegDbQuery;
import ai.classifai.selector.FileSelector;
import ai.classifai.selector.FolderSelector;
import ai.classifai.selector.filesystem.FileSystemStatus;
import ai.classifai.util.ConversionHandler;
import ai.classifai.util.ProjectHandler;
import ai.classifai.util.http.HTTPResponseHandler;
import ai.classifai.util.message.ReplyHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Main server verticle routing different url requests
 *
 * @author Chiawei Lim
 */
@Slf4j
public class ServerVerticle extends AbstractVerticle
{
    private FileSelector fileSelector;
    private FolderSelector folderSelector;

    public ServerVerticle()
    {
        Thread threadFile = new Thread(() -> fileSelector = new FileSelector());
        threadFile.start();

        Thread threadFolder = new Thread(() -> folderSelector = new FolderSelector());
        threadFolder.start();
    }

    /**
     * Get a list of all projects under the category of bounding box
     * PUT http://localhost:{port}/bndbox/projects
     *
     */
    private void getAllBoundingBoxProjects(RoutingContext context)
    {
        JsonObject request = new JsonObject()
                .put(ParamConfig.getAnnotateTypeParam(), AnnotationType.BOUNDINGBOX.ordinal());

        getAllProjects(context, request, AnnotationType.BOUNDINGBOX);
    }

    /**
     * Get a list of all projects under the category of segmentation
     * PUT http://localhost:{port}/seg/projects
     *
     */
    private void getAllSegmentationProjects(RoutingContext context)
    {
        JsonObject request = new JsonObject()
                .put(ParamConfig.getAnnotateTypeParam(), AnnotationType.SEGMENTATION.ordinal());

        getAllProjects(context, request, AnnotationType.SEGMENTATION);
    }

    private void getAllProjects(RoutingContext context, JsonObject request, AnnotationType type)
    {
        DeliveryOptions options = new DeliveryOptions().addHeader(ParamConfig.getActionKeyword(), PortfolioDbQuery.getAllProjectsForAnnotationType());

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
     * PUT http://localhost:{port}/bndbox/newproject/:project_name
     *
     * Example:
     * PUT http://localhost:{port}/bndbox/newproject/helloworld
     *
     */
    private void createBoundingBoxProject(RoutingContext context)
    {
        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());
        JsonObject request = new JsonObject()
                .put(ParamConfig.getProjectNameParam(), projectName)
                .put(ParamConfig.getAnnotateTypeParam(), AnnotationType.BOUNDINGBOX.ordinal());

        createProject(context, request, AnnotationType.BOUNDINGBOX);
    }

    /**
     * Create new project under the category of segmentation
     * PUT http://localhost:{port}/seg/newproject/:project_name
     *
     * Example:
     * PUT http://localhost:{port}/seg/newproject/helloworld
     *
     */
    private void createSegmentationProject(RoutingContext context)
    {
        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());
        JsonObject request = new JsonObject()
                .put(ParamConfig.getProjectNameParam(), projectName)
                .put(ParamConfig.getAnnotateTypeParam(), AnnotationType.SEGMENTATION.ordinal());

        createProject(context, request, AnnotationType.SEGMENTATION);
    }

    private void createProject(RoutingContext context, JsonObject request, AnnotationType annotationType)
    {
        context.request().bodyHandler(h -> {

            DeliveryOptions createOptions = new DeliveryOptions().addHeader(ParamConfig.getActionKeyword(), PortfolioDbQuery.createNewProject());

            vertx.eventBus().request(PortfolioDbQuery.getQueue(), request, createOptions, reply -> {

                if(reply.succeeded())
                {
                    JsonObject response = (JsonObject) reply.result().body();

                    if(ReplyHandler.isReplyOk(response))
                    {
                        HTTPResponseHandler.configureOK(context, response);
                    }
                    else
                    {
                        String projectName = request.getString(ParamConfig.getProjectNameParam());

                        log.info("Failure in creating new " + annotationType.name() +  " project of name: " + projectName);
                        HTTPResponseHandler.configureOK(context, response);
                    }
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
        loadProject(context, BoundingBoxDbQuery.getQueue(), BoundingBoxDbQuery.loadValidProjectUUID(), AnnotationType.BOUNDINGBOX);
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
        loadProject(context, SegDbQuery.getQueue(), SegDbQuery.loadValidProjectUUID(), AnnotationType.SEGMENTATION);
    }

    private void loadProject(RoutingContext context, String queue, String query, AnnotationType annotationType)
    {
        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());

        log.info("Load project: " + projectName + " of annotation type: " + annotationType.name());

        ProjectLoader loader = getProjectLoader(context, projectName, annotationType);

        if(loader == null) return;

        LoaderStatus loaderStatus = loader.getLoaderStatus();

        //Project exist, did not load in ProjectLoader, proceed with loading and checking validity of uuid from database
        if(loaderStatus.equals(LoaderStatus.DID_NOT_INITIATED))
        {
            loader.setLoaderStatus(LoaderStatus.LOADING);

            JsonObject jsonObject = new JsonObject().put(ParamConfig.getProjectIDParam(), loader.getProjectID());

            //load label list
            DeliveryOptions labelOptions = new DeliveryOptions().addHeader(ParamConfig.getActionKeyword(), PortfolioDbQuery.getProjectLabelList());

            vertx.eventBus().request(PortfolioDbQuery.getQueue(), jsonObject, labelOptions, labelReply ->
            {
                if (labelReply.succeeded())
                {
                    JsonObject labelResponse = (JsonObject) labelReply.result().body();

                    if (ReplyHandler.isReplyOk(labelResponse))
                    {
                        //Load label list in ProjectLoader success. Proceed with getting uuid list for processing
                        DeliveryOptions options = new DeliveryOptions().addHeader(ParamConfig.getActionKeyword(), PortfolioDbQuery.getProjectUUIDListt());

                        vertx.eventBus().request(PortfolioDbQuery.getQueue(), jsonObject, options, reply ->
                        {
                            if (reply.succeeded())
                            {
                                JsonObject uuidResponse = (JsonObject) reply.result().body();

                                if (ReplyHandler.isReplyOk(uuidResponse))
                                {
                                    JsonArray uuidListArray = uuidResponse.getJsonArray(ParamConfig.getUUIDListParam());

                                    Integer uuidGeneratorSeed = uuidResponse.getInteger(ParamConfig.getUuidGeneratorParam());

                                    JsonObject uuidListObject = jsonObject.put(ParamConfig.getUUIDListParam(), uuidListArray).put(ParamConfig.getProjectIDParam(), loader.getProjectID()).put(ParamConfig.getUuidGeneratorParam(), uuidGeneratorSeed);

                                    DeliveryOptions uuidListOptions = new DeliveryOptions().addHeader(ParamConfig.getActionKeyword(), query);

                                    //start checking uuid if it's path is still exist
                                    vertx.eventBus().request(queue, uuidListObject, uuidListOptions, fetch ->
                                    {
                                        JsonObject removalResponse = (JsonObject) fetch.result().body();

                                        if (ReplyHandler.isReplyOk(removalResponse))
                                        {
                                            HTTPResponseHandler.configureOK(context, ReplyHandler.getOkReply());

                                        } else
                                        {
                                            HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError("Failed to load project " + projectName + ". Check validity of data points failed."));
                                        }
                                    });
                                }
                                else
                                {
                                    HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError("Failed to load project " + projectName + ". Get project uuid list failed."));
                                }
                            }
                            else
                            {
                                HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError("Failed to load project " + projectName + ". Query database to get project uuid list failed."));
                            }
                        });
                    }
                    else
                    {
                        HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError("Server reply failure message when retrieving uuid list of project " + projectName + ". Loading project aborted."));
                    }
                }
            });
        }
        else if(loaderStatus.equals(LoaderStatus.LOADED))
        {
            loader.setFileSystemStatus(FileSystemStatus.DID_NOT_INITIATE); //reset file system
            HTTPResponseHandler.configureOK(context, ReplyHandler.getOkReply());
        }
        else if(loaderStatus.equals(LoaderStatus.LOADING))
        {
            HTTPResponseHandler.configureOK(context, ReplyHandler.getOkReply());
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

        ProjectLoader projectLoader = getProjectLoader(context, projectName, annotationType);

        if (projectLoader == null) return;

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
            jsonObject.put(ParamConfig.getUUIDListParam(), projectLoader.getSanityUUIDList());

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
        ProjectLoader loader = getProjectLoader(context, projectName, annotationType);

        if(loader == null) return;

        FileSystemStatus fileSystemStatus = loader.getFileSystemStatus();

        if(fileSystemStatus.equals(FileSystemStatus.WINDOW_OPEN))
        {
            HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError("File system processing. Not allowed to proceed"));
        }
        else
        {
            HTTPResponseHandler.configureOK(context, ReplyHandler.getOkReply());

            String fileType = context.request().getParam(ParamConfig.getFileSysParam());

            if(!ProjectHandler.initSelector(fileType))
            {
                JsonObject jsonObject = ReplyHandler.reportUserDefinedError("Filetype with parameter " + fileType + " is not recognizable");
                HTTPResponseHandler.configureOK(context, jsonObject);
                return;
            }

            Integer currentProjectID = loader.getProjectID();

            if (fileType.equals(ParamConfig.getFileParam()))
            {
                fileSelector.run(currentProjectID);
            }
            else if (fileType.equals(ParamConfig.getFolderParam()))
            {
                folderSelector.run(currentProjectID);
            }
            HTTPResponseHandler.configureOK(context, ReplyHandler.getOkReply());
        }
    }

    private ProjectLoader getProjectLoader(RoutingContext context, String projectName, AnnotationType annotationType)
    {
        ProjectLoader projectLoader = ProjectHandler.getProjectLoader(projectName, annotationType);

        if(projectLoader == null)
        {
            String messageInfo = "Project name " + projectName + " cannot be found for loading";
            log.info("Project Loader null. " + messageInfo);
            HTTPResponseHandler.configureOK(context, ReplyHandler.reportProjectNameError(messageInfo));
            return null;
        }

        return projectLoader;
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
    public void getBndBoxFileSystemStatus(RoutingContext context)
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
    public void getSegFileSystemStatus(RoutingContext context)
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
    public void getFileSystemStatus(RoutingContext context, AnnotationType annotationType) {

        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());

        ProjectLoader loader = ProjectHandler.getProjectLoader(projectName, annotationType);
        FileSystemStatus fileSysStatus = loader.getFileSystemStatus();

        JsonObject res = new JsonObject().put(ReplyHandler.getMessageKey(), fileSysStatus.ordinal());

        if(fileSysStatus.equals(FileSystemStatus.WINDOW_CLOSE_DATABASE_UPDATING))
        {
            res.put(ParamConfig.getProgressMetadata(), loader.getProgressUpdate());
        }
        else if(fileSysStatus.equals(FileSystemStatus.WINDOW_CLOSE_DATABASE_UPDATED) | (fileSysStatus.equals(FileSystemStatus.WINDOW_CLOSE_DATABASE_NOT_UPDATED)))
        {
            List<Integer> newAddedUUIDList = loader.getFileSysNewUUIDList();

            res.put(ParamConfig.getUUIDListParam(), newAddedUUIDList);

        }

        HTTPResponseHandler.configureOK(context, res);
    }

    /**
     * Create new label for bounding box annotation project
     *
     * PUT http://localhost:{port}/seg/projects/:project_name/newlabels
     */
    private void createNewBndBoxLabels(RoutingContext context)
    {
        createNewLabels(context, AnnotationType.BOUNDINGBOX);
    }

    /**
     * Create new label for segmentation annotation project
     *
     * PUT http://localhost:{port}/seg/projects/:project_name/newlabels
     */
    private void createNewSegLabels(RoutingContext context)
    {
        createNewLabels(context, AnnotationType.SEGMENTATION);
    }

    /**
     * Create new label for projects
     *
     * PUT http://localhost:{port}/bndbox/projects/:project_name/newlabels
     * PUT http://localhost:{port}/seg/projects/:project_name/newlabels
     *
     * Example:
     * PUT http://localhost:{port}/bndbox/projects/helloworld/newlabels
     */
    private void createNewLabels(RoutingContext context, AnnotationType annotationType)
    {
        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());

        Integer projectID = ProjectHandler.getProjectID(projectName, annotationType.ordinal());

        context.request().bodyHandler(h ->
        {
            io.vertx.core.json.JsonObject jsonObject = ConversionHandler.json2JSONObject(h.toJson());

            jsonObject.put(ParamConfig.getProjectIDParam(), projectID);

            DeliveryOptions options = new DeliveryOptions().addHeader(ParamConfig.getActionKeyword(), PortfolioDbQuery.updateLabelList());

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
     * Retrieve thumbnail with metadata
     *
     * GET http://localhost:{port}/bndbox/projects/:project_name/uuid/:uuid/thumbnail
     *
     */
    public void getBndBoxMetadata(RoutingContext context)
    {
        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());
        Integer projectID = ProjectHandler.getProjectID(projectName, AnnotationType.BOUNDINGBOX.ordinal());
        Integer uuid = Integer.parseInt(context.request().getParam(ParamConfig.getUUIDParam()));

        JsonObject request = new JsonObject().put(ParamConfig.getUUIDParam(), uuid)
                .put(ParamConfig.getProjectIDParam(), projectID)
                .put(ParamConfig.getProjectNameParam(), projectName);

        getMetadata(context, BoundingBoxDbQuery.getQueue(), BoundingBoxDbQuery.retrieveData(), request);
    }

    /**
     * Retrieve thumbnail with metadata
     *
     * GET http://localhost:{port}/seg/projects/:project_name/uuid/:uuid/thumbnail
     *
     */
    public void getSegMetadata(RoutingContext context)
    {
        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());
        Integer projectID = ProjectHandler.getProjectID(projectName, AnnotationType.SEGMENTATION.ordinal());
        Integer uuid = Integer.parseInt(context.request().getParam(ParamConfig.getUUIDParam()));

        JsonObject request = new JsonObject().put(ParamConfig.getUUIDParam(), uuid)
                .put(ParamConfig.getProjectIDParam(), projectID)
                .put(ParamConfig.getProjectNameParam(), projectName);

        getMetadata(context, SegDbQuery.getQueue(), SegDbQuery.retrieveData(), request);
    }

    public void getMetadata(RoutingContext context, String queue, String query, JsonObject request)
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
    public void getBndBoxImageSource(RoutingContext context) {
        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());
        Integer projectID = ProjectHandler.getProjectID(projectName, AnnotationType.BOUNDINGBOX.ordinal());
        Integer uuid = Integer.parseInt(context.request().getParam(ParamConfig.getUUIDParam()));

        JsonObject request = new JsonObject().put(ParamConfig.getUUIDParam(), uuid)
                .put(ParamConfig.getProjectIDParam(), projectID);

        getImageSource(context, BoundingBoxDbQuery.getQueue(), BoundingBoxDbQuery.retrieveDataPath(), request);
    }

    /***
     *
     * Get Image Source
     *
     * GET http://localhost:{port}/seg/projects/:project_name/uuid/:uuid/imgsrc
     *
     */
    public void getSegImageSource(RoutingContext context) {
        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());
        Integer projectID = ProjectHandler.getProjectID(projectName, AnnotationType.SEGMENTATION.ordinal());
        Integer uuid = Integer.parseInt(context.request().getParam(ParamConfig.getUUIDParam()));

        JsonObject request = new JsonObject().put(ParamConfig.getUUIDParam(), uuid)
                .put(ParamConfig.getProjectIDParam(), projectID);

        getImageSource(context, SegDbQuery.getQueue(), SegDbQuery.retrieveDataPath(), request);
    }

    public void getImageSource(RoutingContext context, String queue, String query, JsonObject request)
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

        context.request().bodyHandler(h ->
        {
            DeliveryOptions updateOptions = new DeliveryOptions().addHeader(ParamConfig.getActionKeyword(), BoundingBoxDbQuery.updateData());

            io.vertx.core.json.JsonObject jsonObject = ConversionHandler.json2JSONObject(h.toJson());
            jsonObject.put(ParamConfig.getProjectIDParam(), ProjectHandler.getProjectID(projectName, AnnotationType.BOUNDINGBOX.ordinal()));

            vertx.eventBus().request(BoundingBoxDbQuery.getQueue(), jsonObject, updateOptions, fetch ->
            {
                if (fetch.succeeded()) {
                    JsonObject response = (JsonObject) fetch.result().body();

                    HTTPResponseHandler.configureOK(context, response);

                }
                else {
                    HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError("Failure in updating database for bounding box project."));
                }
            });
        });
    }

    /***
     *
     * Update segmentation labelling information
     *
     * PUT http://localhost:{port}/seg/projects/:project_name/uuid/:uuid/update
     *
     */
    private void updateSegData(RoutingContext context)
    {
        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());

        context.request().bodyHandler(h ->
        {
            DeliveryOptions updateOptions = new DeliveryOptions().addHeader(ParamConfig.getActionKeyword(), SegDbQuery.updateData());

            io.vertx.core.json.JsonObject jsonObject = ConversionHandler.json2JSONObject(h.toJson());
            jsonObject.put(ParamConfig.getProjectIDParam(), ProjectHandler.getProjectID(projectName, AnnotationType.SEGMENTATION.ordinal()));

            vertx.eventBus().request(SegDbQuery.getQueue(), jsonObject, updateOptions, fetch ->
            {
                if (fetch.succeeded()) {
                    JsonObject response = (JsonObject) fetch.result().body();

                    HTTPResponseHandler.configureOK(context, response);

                }
                else {
                    HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError("Failure in updating database for segmentation project."));
                }
            });
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
        Integer projectID = ProjectHandler.getProjectID(projectName, annotationType.ordinal());

        context.request().bodyHandler(h ->
        {
            io.vertx.core.json.JsonObject jsonObject = ConversionHandler.json2JSONObject(h.toJson());

            jsonObject.put(ParamConfig.getProjectIDParam(), projectID);

            DeliveryOptions options = new DeliveryOptions().addHeader(ParamConfig.getActionKeyword(), PortfolioDbQuery.updateLabelList());

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


    @Override
    public void start(Promise<Void> promise)
    {
        Router router = Router.router(vertx);

        //display for content in webroot
        router.route().handler(StaticHandler.create());

        //*******************************Bounding Box*******************************

        router.get("/bndbox/projects").handler(this::getAllBoundingBoxProjects);

        router.put("/bndbox/newproject/:project_name").handler(this::createBoundingBoxProject);

        router.get("/bndbox/projects/:project_name").handler(this::loadBndBoxProject);

        router.get("/bndbox/projects/:project_name/loadingstatus").handler(this::loadBndBoxProjectStatus);

        router.get("/bndbox/projects/:project_name/filesys/:file_sys").handler(this::selectBndBoxFileSystemType);

        router.get("/bndbox/projects/:project_name/filesysstatus").handler(this::getBndBoxFileSystemStatus);

        router.put("/bndbox/projects/:project_name/newlabels").handler(this::createNewBndBoxLabels);

        router.get("/bndbox/projects/:project_name/uuid/:uuid/thumbnail").handler(this::getBndBoxMetadata);

        router.get("/bndbox/projects/:project_name/uuid/:uuid/imgsrc").handler(this::getBndBoxImageSource);

        router.put("/bndbox/projects/:project_name/uuid/:uuid/update").handler(this::updateBndBoxData);

        router.put("/bndbox/projects/:project_name/newlabels").handler(this::updateBndBoxLabels);

        //*******************************Segmentation*******************************

        router.get("/seg/projects").handler(this::getAllSegmentationProjects);

        router.put("/seg/newproject/:project_name").handler(this::createSegmentationProject);

        router.get("/seg/projects/:project_name").handler(this::loadSegProject);

        router.get("/seg/projects/:project_name/loadingstatus").handler(this::loadSegProjectStatus);

        router.get("/seg/projects/:project_name/filesys/:file_sys").handler(this::selectSegFileSystemType);

        router.get("/seg/projects/:project_name/filesysstatus").handler(this::getSegFileSystemStatus);

        router.put("/seg/projects/:project_name/newlabels").handler(this::createNewSegLabels);

        router.get("/seg/projects/:project_name/uuid/:uuid/thumbnail").handler(this::getSegMetadata);

        router.get("/seg/projects/:project_name/uuid/:uuid/imgsrc").handler(this::getSegImageSource);

        router.put("/seg/projects/:project_name/uuid/:uuid/update").handler(this::updateSegData);

        router.put("/seg/projects/:project_name/newlabels").handler(this::updateSegLabels);

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
