/*
 * Copyright (c) 2020 CertifAI
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
import ai.classifai.database.loader.LoaderStatus;
import ai.classifai.database.loader.ProjectLoader;
import ai.classifai.database.portfoliodb.PortfolioDbQuery;
import ai.classifai.database.boundingboxdb.BoundingBoxDbQuery;
import ai.classifai.database.segdb.SegDbQuery;
import ai.classifai.selector.FileSelector;
import ai.classifai.selector.FolderSelector;
import ai.classifai.selector.SelectorHandler;
import ai.classifai.selector.SelectorStatus;
import ai.classifai.util.ConversionHandler;
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
import java.util.concurrent.atomic.AtomicInteger;

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
        Thread threadfile = new Thread(){
            public void run(){
                fileSelector = new FileSelector();
            }
        };
        threadfile.start();

        Thread threadfolder = new Thread(){
            public void run(){
                folderSelector = new FolderSelector();
            }
        };
        threadfolder.start();
    }

    /**
     * Get a list of all projects under the category of bounding box
     * PUT http://localhost:{port}/bndbox/projects
     *
     */
    private void getAllBoundingBoxProjects(RoutingContext context)
    {
        JsonObject request = new JsonObject()
                .put(ParamConfig.ANNOTATE_TYPE_PARAM, AnnotationType.BOUNDINGBOX.ordinal());

        getAllProjects(context, request);
    }

    /**
     * Get a list of all projects under the category of segmentation
     * PUT http://localhost:{port}/seg/projects
     *
     */
    private void getAllSegmentationProjects(RoutingContext context)
    {
        JsonObject request = new JsonObject()
                .put(ParamConfig.ANNOTATE_TYPE_PARAM, AnnotationType.SEGMENTATION.ordinal());

        getAllProjects(context, request);
    }

    private void getAllProjects(RoutingContext context, JsonObject request)
    {
        DeliveryOptions options = new DeliveryOptions().addHeader(ParamConfig.ACTION_KEYWORD, PortfolioDbQuery.GET_ALL_PROJECTS_FOR_ANNOTATION_TYPE);

        vertx.eventBus().request(PortfolioDbQuery.QUEUE, request, options, reply -> {

            if(reply.succeeded())
            {
                JsonObject response = (JsonObject) reply.result().body();

                if(ReplyHandler.isReplyOk(response))
                {
                    HTTPResponseHandler.configureOK(context, response);
                }
                else {
                    //soft fail
                    HTTPResponseHandler.configureOK(context, response);
                }
            }
            else
            {
                HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError("Warning: Failed in getting all the projects"));
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
        String projectName = context.request().getParam(ParamConfig.PROJECT_NAME_PARAM);
        JsonObject request = new JsonObject()
                .put(ParamConfig.PROJECT_NAME_PARAM, projectName)
                .put(ParamConfig.ANNOTATE_TYPE_PARAM, AnnotationType.BOUNDINGBOX.ordinal());

        createProject(context, request);
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
        String projectName = context.request().getParam(ParamConfig.PROJECT_NAME_PARAM);
        JsonObject request = new JsonObject()
                .put(ParamConfig.PROJECT_NAME_PARAM, projectName)
                .put(ParamConfig.ANNOTATE_TYPE_PARAM, AnnotationType.SEGMENTATION.ordinal());

        createProject(context, request);
    }

    private void createProject(RoutingContext context, JsonObject request)
    {
        context.request().bodyHandler(h -> {

            DeliveryOptions createOptions = new DeliveryOptions().addHeader(ParamConfig.ACTION_KEYWORD, PortfolioDbQuery.CREATE_NEW_PROJECT);

            vertx.eventBus().request(PortfolioDbQuery.QUEUE, request, createOptions, reply -> {

                if(reply.succeeded())
                {
                    JsonObject response = (JsonObject) reply.result().body();

                    if(ReplyHandler.isReplyOk(response))
                    {
                        HTTPResponseHandler.configureOK(context, response);
                    }
                    else
                    {
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
        loadProject(context, BoundingBoxDbQuery.QUEUE, BoundingBoxDbQuery.REMOVE_OBSOLETE_UUID_LIST);
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
        loadProject(context, SegDbQuery.QUEUE, SegDbQuery.REMOVE_OBSOLETE_UUID_LIST);
    }

    private void loadProject(RoutingContext context, String queue, String query)
    {
        String projectName = context.request().getParam(ParamConfig.PROJECT_NAME_PARAM);

        ProjectLoader loader = SelectorHandler.getProjectLoader(projectName);

        if (loader == null) {
            HTTPResponseHandler.configureBadRequest(context, ReplyHandler.reportProjectNameError());
            return;
        }

        LoaderStatus loaderStatus = loader.getLoaderStatus();

        if (loaderStatus != LoaderStatus.LOADING) {

            loader.setLoaderStatus(LoaderStatus.LOADING);

            JsonObject jsonObject = new JsonObject().put(ParamConfig.PROJECT_NAME_PARAM, projectName);

            //load label list
            DeliveryOptions labelOptions = new DeliveryOptions().addHeader(ParamConfig.ACTION_KEYWORD, PortfolioDbQuery.GET_PROJECT_LABEL_LIST);
            vertx.eventBus().request(PortfolioDbQuery.QUEUE, jsonObject, labelOptions, labelReply ->
            {
                if (labelReply.succeeded()) {
                    JsonObject labelResponse = (JsonObject) labelReply.result().body();

                    if (ReplyHandler.isReplyOk(labelResponse))
                    {
                        //Load label list in ProjectLoader success. Proceed with getting uuid list for processing
                        DeliveryOptions options = new DeliveryOptions().addHeader(ParamConfig.ACTION_KEYWORD, PortfolioDbQuery.GET_PROJECT_UUID_LIST);

                        vertx.eventBus().request(PortfolioDbQuery.QUEUE, jsonObject, options, reply ->
                        {
                            if (reply.succeeded())
                            {
                                JsonObject oriUUIDResponse = (JsonObject) reply.result().body();

                                if (ReplyHandler.isReplyOk(oriUUIDResponse)) {
                                    JsonArray uuidListArray = oriUUIDResponse.getJsonArray(ParamConfig.UUID_LIST_PARAM);
                                    JsonObject removalObject = jsonObject.put(ParamConfig.UUID_LIST_PARAM, uuidListArray);

                                    DeliveryOptions removalOptions = new DeliveryOptions().addHeader(ParamConfig.ACTION_KEYWORD, query);

                                    //start checking uuid if it's path is still exist
                                    vertx.eventBus().request(queue, removalObject, removalOptions, fetch ->
                                    {
                                        JsonObject removalResponse = (JsonObject) fetch.result().body();

                                        if (ReplyHandler.isReplyOk(removalResponse)) {
                                            log.info("Loading project: " + projectName);

                                        } else {
                                            log.info("Failed to load project: " + projectName);
                                        }
                                    });
                                } else {
                                    log.error("Project " + projectName + ": Get project uuid list failed. In the process of removing obsolete uuid list. ");
                                }
                            }
                            else
                            {

                            }
                        });
                        HTTPResponseHandler.configureOK(context, ReplyHandler.getOkReply()); //FIXME: This is terrible
                    }
                    else
                    {
                        HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError("Get label list for project failed. Loading project aborted."));
                    }
                } else {
                    HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError("Failed to query server for label list when loading project. Loading project aborted"));
                }
            });
        }
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
    private void loadProjectStatus(RoutingContext context)
    {
        String projectName = context.request().getParam(ParamConfig.PROJECT_NAME_PARAM);

        ProjectLoader projectLoader = SelectorHandler.getProjectLoader(projectName);

        if(projectLoader == null)
        {
            HTTPResponseHandler.configureBadRequest(context,  ReplyHandler.reportProjectNameError());
            return;
        }

        LoaderStatus loaderStatus = projectLoader.getLoaderStatus();

        if(loaderStatus == LoaderStatus.ERROR)
        {
            HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError("Loader status error. Something wrong."));
        }
        else if(loaderStatus == LoaderStatus.LOADING)
        {
            JsonObject jsonObject = ReplyHandler.getOkReply();
            jsonObject.put(ParamConfig.PROGRESS_METADATA, projectLoader.getProgress());

            HTTPResponseHandler.configureOK(context, jsonObject);
        }
        else if((loaderStatus == LoaderStatus.LOADED)  || (loaderStatus == LoaderStatus.EMPTY))
        {
            //uuidList
            //labelList
            //ok response
            projectLoader.resetLoaderStatus();
            /*
            DeliveryOptions options = new DeliveryOptions().addHeader(ParamConfig.ACTION_KEYWORD, PortfolioDbQuery.GET_UUID_LABEL_LIST);

            vertx.eventBus().request(PortfolioDbQuery.QUEUE, new JsonObject().put(ParamConfig.PROJECT_NAME_PARAM, projectName), options, reply ->
            {
                if (reply.succeeded()) {

                    JsonObject response = (JsonObject) reply.result().body();

                    if(ReplyHandler.isReplyOk(response))
                    {
                        response.put(ReplyHandler.getMessageKey(), LoaderStatus.LOADED.ordinal());

                        //reset
                        projectLoader.resetLoaderStatus();

                        HTTPResponseHandler.configureOK(context, response);
                    }
                    else
                    {
                        HTTPResponseHandler.configureOK(context, response);
                    }

                } else {
                    HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError("Failed to get reply for uuid label list"));
                }
            });

            */
        }
        else if(loaderStatus == LoaderStatus.DID_NOT_INITIATED)
        {
            JsonObject object = new JsonObject();
            object.put(ReplyHandler.getMessageKey(), LoaderStatus.DID_NOT_INITIATED.ordinal());

            HTTPResponseHandler.configureOK(context, object);
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
        selectFileSystemType(context, AnnotationType.BOUNDINGBOX);
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
        selectFileSystemType(context, AnnotationType.SEGMENTATION);
    }

    private void selectFileSystemType(RoutingContext context, AnnotationType annotationType)
    {
        if(SelectorHandler.isLoaderProcessing()) //file or folder selecting and updating
        {
            JsonObject jsonObject = new JsonObject();
            jsonObject.put(ReplyHandler.getMessageKey(), 2);
            jsonObject.put(ReplyHandler.getErrorMesageKey(), "Database is updating");

            HTTPResponseHandler.configureOK(context, jsonObject);
        }
        else if (SelectorHandler.isWindowOpen())
        {
            JsonObject jsonObject = new JsonObject();
            jsonObject.put(ReplyHandler.getMessageKey(), 2);
            jsonObject.put(ReplyHandler.getErrorMesageKey(), "Window is open");

            HTTPResponseHandler.configureOK(context, jsonObject);
        }
        else
        {
            String projectName = context.request().getParam(ParamConfig.PROJECT_NAME_PARAM);
            String fileType = context.request().getParam(ParamConfig.FILE_SYS_PARAM);

            if(SelectorHandler.isProjectNameRegistered(projectName) == false)
            {
                HTTPResponseHandler.configureOK(context, ReplyHandler.reportProjectNameError());
            }
            else
            {
                SelectorHandler.setProjectNameBuffer(projectName);

                boolean isFileTypeSupported = SelectorHandler.initSelector(fileType);

                if(!isFileTypeSupported)
                {
                    JsonObject jsonObject = ReplyHandler.reportUserDefinedError("filetype with parameter " + fileType + " which is not recognizable");
                    jsonObject.put(ReplyHandler.getMessageKey(), ReplyHandler.getFailedSignal());

                    HTTPResponseHandler.configureOK(context, jsonObject);
                }
                else
                {
                    //set uuid generator
                    DeliveryOptions options = new DeliveryOptions().addHeader(ParamConfig.ACTION_KEYWORD, PortfolioDbQuery.GET_PROJECT_UUID_LIST);
                    vertx.eventBus().request(PortfolioDbQuery.QUEUE, new JsonObject().put(ParamConfig.PROJECT_NAME_PARAM, projectName), options, reply ->
                    {
                        if(reply.succeeded())
                        {
                            JsonObject response = (JsonObject) reply.result().body();

                            if(ReplyHandler.isReplyOk(response))
                            {
                                //this is to initiate the generator id to the end of existing list
                                List<Integer> uuidList = ConversionHandler.jsonArray2IntegerList(response.getJsonArray(ParamConfig.UUID_LIST_PARAM));

                                Integer seedNumber = uuidList.isEmpty() ? 0 : uuidList.size() + 1;

                                SelectorHandler.configureOpenWindow(projectName, seedNumber);

                                if (fileType.equals(SelectorHandler.FILE))
                                {
                                    fileSelector.runFileSelector(annotationType, projectName, new AtomicInteger(seedNumber));
                                }
                                else if (fileType.equals(SelectorHandler.FOLDER))
                                {
                                    folderSelector.runFolderSelector(annotationType, projectName, new AtomicInteger(seedNumber));
                                }

                                HTTPResponseHandler.configureOK(context, ReplyHandler.getOkReply());
                            }
                            else
                            {
                                HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError("Warning: Initiate generator failed, not able to initate file/folder selector"));
                                //HTTPResponseHandler.configureBadRequest(context, response);
                                return;
                            }
                        }
                    });
                }
            }
        }
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
    public void getFileSystemStatus(RoutingContext context) {

        String projectName = context.request().getParam(ParamConfig.PROJECT_NAME_PARAM);

        //check project name if exist
        if(SelectorHandler.isProjectNameRegistered(projectName) == false)
        {
            JsonObject object = new JsonObject();
            object.put(ReplyHandler.getMessageKey(), SelectorStatus.ERROR.ordinal());
            object.put(ReplyHandler.getErrorMesageKey(), "Project name did not exist");
            HTTPResponseHandler.configureOK(context, object);
        }
        else
        {
            if(SelectorHandler.isWindowOpen())
            {
                HTTPResponseHandler.configureOK(context, new JsonObject().put(ReplyHandler.getMessageKey(), SelectorStatus.WINDOW_OPEN.ordinal()));
            }
            else if (SelectorHandler.isLoaderProcessing())
            {
                JsonObject res = new JsonObject();

                SelectorStatus selectorStatus = SelectorHandler.getSelectorStatus();

                res.put(ParamConfig.PROGRESS_METADATA, SelectorHandler.getProgressUpdate(projectName));
                res.put(ReplyHandler.getMessageKey(), selectorStatus.ordinal());

                HTTPResponseHandler.configureOK(context, res);
            }
            else {
                JsonObject request = new JsonObject().put(ParamConfig.PROJECT_NAME_PARAM, projectName);

                DeliveryOptions options = new DeliveryOptions().addHeader(ParamConfig.ACTION_KEYWORD, PortfolioDbQuery.GET_THUMBNAIL_LIST);

                vertx.eventBus().request(PortfolioDbQuery.QUEUE, request, options, reply ->
                {
                    if(reply.succeeded())
                    {
                        JsonObject response = (JsonObject) reply.result().body();

                        if(ReplyHandler.isReplyOk(response))
                        {
                            List<Integer> intList = ConversionHandler.jsonArray2IntegerList(response.getJsonArray(ParamConfig.UUID_LIST_PARAM));

                            if(intList.isEmpty())
                            {
                                response.put(ReplyHandler.getMessageKey(), SelectorStatus.WINDOW_CLOSE_DATABASE_NOT_UPDATED.ordinal());
                                HTTPResponseHandler.configureOK(context, response);
                            }
                            else
                            {
                                response.put(ReplyHandler.getMessageKey(), SelectorStatus.WINDOW_CLOSE_DATABASE_UPDATED.ordinal());
                                HTTPResponseHandler.configureOK(context, response);
                            }
                        }
                        else
                        {
                            //temporary fix to fit this function
                            response.put(ReplyHandler.getMessageKey(), SelectorStatus.ERROR.ordinal());
                            HTTPResponseHandler.configureOK(context, response);
                        }
                    }
                    else
                    {
                        JsonObject object = new JsonObject();

                        object.put(ReplyHandler.getMessageKey(), SelectorStatus.ERROR.ordinal());
                        object.put(ReplyHandler.getErrorMesageKey(), "Failed in getting thumbnail list");

                        HTTPResponseHandler.configureOK(context, object);
                    }
                });
            }
        }
    }

    /**
     * Create new label for bounding box annotation project
     *
     * PUT http://localhost:{port}/bndbox/projects/:project_name/newlabels
     * PUT http://localhost:{port}/seg/projects/:project_name/newlabels
     *
     * Example:
     * PUT http://localhost:{port}/bndbox/projects/helloworld/newlabels
     */
    private void createNewLabel(RoutingContext context)
    {
        String projectName = context.request().getParam(ParamConfig.PROJECT_NAME_PARAM);

        context.request().bodyHandler(h ->
        {
            io.vertx.core.json.JsonObject jsonObject = ConversionHandler.json2JSONObject(h.toJson());

            jsonObject.put(ParamConfig.PROJECT_NAME_PARAM, projectName);

            DeliveryOptions options = new DeliveryOptions().addHeader(ParamConfig.ACTION_KEYWORD, PortfolioDbQuery.UPDATE_LABEL);

            vertx.eventBus().request(PortfolioDbQuery.QUEUE, jsonObject, options, reply ->
            {
                if(reply.succeeded())
                {
                    JsonObject response = (JsonObject) reply.result().body();

                    if (ReplyHandler.isReplyOk(response))
                    {
                        HTTPResponseHandler.configureOK(context, response);
                    }
                    else
                    {
                        //soft fail
                        HTTPResponseHandler.configureOK(context, response);
                    }
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
        String projectName = context.request().getParam(ParamConfig.PROJECT_NAME_PARAM);
        Integer uuid = Integer.parseInt(context.request().getParam(ParamConfig.UUID_PARAM));

        JsonObject request = new JsonObject().put(ParamConfig.UUID_PARAM, uuid)
                .put(ParamConfig.PROJECT_NAME_PARAM, projectName);

        getMetadata(context, BoundingBoxDbQuery.QUEUE, BoundingBoxDbQuery.RETRIEVE_DATA, request);
    }

    /**
     * Retrieve thumbnail with metadata
     *
     * GET http://localhost:{port}/seg/projects/:project_name/uuid/:uuid/thumbnail
     *
     */
    public void getSegMetadata(RoutingContext context)
    {
        String projectName = context.request().getParam(ParamConfig.PROJECT_NAME_PARAM);
        Integer uuid = Integer.parseInt(context.request().getParam(ParamConfig.UUID_PARAM));

        JsonObject request = new JsonObject().put(ParamConfig.UUID_PARAM, uuid)
                .put(ParamConfig.PROJECT_NAME_PARAM, projectName);

        getMetadata(context, SegDbQuery.QUEUE, SegDbQuery.RETRIEVE_DATA, request);
    }

    public void getMetadata(RoutingContext context, String queue, String query, JsonObject request)
    {

        DeliveryOptions thumbnailOptions = new DeliveryOptions().addHeader(ParamConfig.ACTION_KEYWORD, query);

        vertx.eventBus().request(queue, request, thumbnailOptions, fetch -> {

            if (fetch.succeeded())
            {
                JsonObject result = (JsonObject) fetch.result().body();

                if(ReplyHandler.isReplyOk(result))
                {
                    HTTPResponseHandler.configureOK(context, result);

                }
                else {
                    HTTPResponseHandler.configureOK(context, result);
                }

            }
            else
            {
                HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError("Warning: Failed in retrieving thumbnail"));
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
        String projectName = context.request().getParam(ParamConfig.PROJECT_NAME_PARAM);
        Integer uuid = Integer.parseInt(context.request().getParam(ParamConfig.UUID_PARAM));

        JsonObject request = new JsonObject().put(ParamConfig.UUID_PARAM, uuid)
                .put(ParamConfig.PROJECT_NAME_PARAM, projectName);

        getImageSource(context, BoundingBoxDbQuery.QUEUE, BoundingBoxDbQuery.RETRIEVE_DATA_PATH, request);
    }

    /***
     *
     * Get Image Source
     *
     * GET http://localhost:{port}/seg/projects/:project_name/uuid/:uuid/imgsrc
     *
     */
    public void getSegImageSource(RoutingContext context) {
        String projectName = context.request().getParam(ParamConfig.PROJECT_NAME_PARAM);
        Integer uuid = Integer.parseInt(context.request().getParam(ParamConfig.UUID_PARAM));

        JsonObject request = new JsonObject().put(ParamConfig.UUID_PARAM, uuid)
                .put(ParamConfig.PROJECT_NAME_PARAM, projectName);

        getImageSource(context, SegDbQuery.QUEUE, SegDbQuery.RETRIEVE_DATA_PATH, request);
    }

    public void getImageSource(RoutingContext context, String queue, String query, JsonObject request)
    {
        DeliveryOptions imgSrcOptions = new DeliveryOptions().addHeader(ParamConfig.ACTION_KEYWORD, query);

        vertx.eventBus().request(queue, request, imgSrcOptions, fetch -> {

            if (fetch.succeeded()) {

                JsonObject result = (JsonObject) fetch.result().body();

                if(ReplyHandler.isReplyOk(result))
                {
                    HTTPResponseHandler.configureOK(context, result);
                }
                else
                {
                    //soft fail
                    HTTPResponseHandler.configureOK(context, result);
                    //HTTPResponseHandler.configureBadRequest(context, result);
                }

            }
            else {
                HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError("Warning: Failed to get image source."));
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
        context.request().bodyHandler(h ->
        {
            DeliveryOptions updateOptions = new DeliveryOptions().addHeader(ParamConfig.ACTION_KEYWORD, BoundingBoxDbQuery.UPDATE_DATA);

            io.vertx.core.json.JsonObject jsonObject = ConversionHandler.json2JSONObject(h.toJson());

            vertx.eventBus().request(BoundingBoxDbQuery.QUEUE, jsonObject, updateOptions, fetch ->
            {
                if (fetch.succeeded()) {
                    JsonObject response = (JsonObject) fetch.result().body();

                    if(ReplyHandler.isReplyOk(response))
                    {
                        HTTPResponseHandler.configureOK(context, response);
                    }
                    else
                    {
                        //soft fail
                        HTTPResponseHandler.configureOK(context, response);
                        //HTTPResponseHandler.configureBadRequest(context, response);
                    }

                }
                else {
                    HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError("Warning: Update database failed"));
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
        context.request().bodyHandler(h ->
        {
            DeliveryOptions updateOptions = new DeliveryOptions().addHeader(ParamConfig.ACTION_KEYWORD, SegDbQuery.UPDATE_DATA);

            io.vertx.core.json.JsonObject jsonObject = ConversionHandler.json2JSONObject(h.toJson());

            vertx.eventBus().request(SegDbQuery.QUEUE, jsonObject, updateOptions, fetch ->
            {
                if (fetch.succeeded()) {
                    JsonObject response = (JsonObject) fetch.result().body();

                    if(ReplyHandler.isReplyOk(response))
                    {
                        HTTPResponseHandler.configureOK(context, response);
                    }
                    else
                    {
                        //soft fail
                        HTTPResponseHandler.configureOK(context, response);
                        //HTTPResponseHandler.configureBadRequest(context, response);
                    }

                }
                else {
                    HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError("Warning: Update database failed"));
                }
            });
        });
    }

    private void updateData(RoutingContext context, String queue, String query, JsonObject request)
    {

        context.request().bodyHandler(h ->
        {
            DeliveryOptions updateOptions = new DeliveryOptions().addHeader(ParamConfig.ACTION_KEYWORD, query);

            io.vertx.core.json.JsonObject jsonObject = ConversionHandler.json2JSONObject(h.toJson());

            vertx.eventBus().request(queue, jsonObject, updateOptions, fetch ->
            {
                if (fetch.succeeded()) {
                    JsonObject response = (JsonObject) fetch.result().body();

                    if(ReplyHandler.isReplyOk(response))
                    {
                        HTTPResponseHandler.configureOK(context, response);
                    }
                    else
                    {
                        //soft fail
                        HTTPResponseHandler.configureOK(context, response);
                        //HTTPResponseHandler.configureBadRequest(context, response);
                    }

                }
                else {
                    HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError("Warning: Update database failed"));
                }
            });
        });
    }

    /***
     *
     * Update labels
     *
     * PUT http://localhost:{port}/bndbox/projects/:project_name/newlabels
     * PUT http://localhost:{port}/seg/projects/:project_name/newlabels
     *
     */
    private void updateLabels(RoutingContext context)
    {
        String projectName = context.request().getParam(ParamConfig.PROJECT_NAME_PARAM);

        context.request().bodyHandler(h ->
        {
            io.vertx.core.json.JsonObject jsonObject = ConversionHandler.json2JSONObject(h.toJson());

            jsonObject.put(ParamConfig.PROJECT_NAME_PARAM, projectName);

            DeliveryOptions options = new DeliveryOptions().addHeader(ParamConfig.ACTION_KEYWORD, PortfolioDbQuery.UPDATE_LABEL);

            vertx.eventBus().request(PortfolioDbQuery.QUEUE, jsonObject, options, reply ->
            {
                if(reply.succeeded())
                {
                    JsonObject response = (JsonObject) reply.result().body();

                    if (ReplyHandler.isReplyOk(response))
                    {
                        HTTPResponseHandler.configureOK(context, response);
                    }
                    else
                    {
                        //soft fail
                        HTTPResponseHandler.configureOK(context, response);
                    }
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

        router.get("/bndbox/projects/:project_name/loadingstatus").handler(this::loadProjectStatus);

        router.get("/bndbox/projects/:project_name/filesys/:file_sys").handler(this::selectBndBoxFileSystemType);

        router.get("/bndbox/projects/:project_name/filesysstatus").handler(this::getFileSystemStatus);

        router.put("/bndbox/projects/:project_name/newlabels").handler(this::createNewLabel);

        router.get("/bndbox/projects/:project_name/uuid/:uuid/thumbnail").handler(this::getBndBoxMetadata);

        router.get("/bndbox/projects/:project_name/uuid/:uuid/imgsrc").handler(this::getBndBoxImageSource);

        router.put("/bndbox/projects/:project_name/uuid/:uuid/update").handler(this::updateBndBoxData);

        router.put("/bndbox/projects/:project_name/newlabels").handler(this::updateLabels);

        //*******************************Segmentation*******************************

        router.get("/seg/projects").handler(this::getAllSegmentationProjects);

        router.put("/seg/newproject/:project_name").handler(this::createSegmentationProject);

        router.get("/seg/projects/:project_name").handler(this::loadSegProject);

        router.get("/seg/projects/:project_name/loadingstatus").handler(this::loadProjectStatus);

        router.get("/seg/projects/:project_name/filesys/:file_sys").handler(this::selectSegFileSystemType);

        router.get("/seg/projects/:project_name/filesysstatus").handler(this::getFileSystemStatus);

        router.put("/seg/projects/:project_name/newlabels").handler(this::createNewLabel);

        router.get("/seg/projects/:project_name/uuid/:uuid/thumbnail").handler(this::getSegMetadata);

        router.get("/seg/projects/:project_name/uuid/:uuid/imgsrc").handler(this::getSegImageSource);

        router.put("/seg/projects/:project_name/uuid/:uuid/update").handler(this::updateSegData);

        router.put("/seg/projects/:project_name/newlabels").handler(this::updateLabels);

        vertx.createHttpServer()
                .requestHandler(router)
                .exceptionHandler(Throwable::printStackTrace)
                .listen(ParamConfig.getHostingPort(), r -> {
                    if (r.succeeded())
                    {
                        promise.complete();
                    }
                    else {

                        log.debug("HTTPServer failed to start");
                        promise.fail(r.cause());
                    }
                });
    }
}
