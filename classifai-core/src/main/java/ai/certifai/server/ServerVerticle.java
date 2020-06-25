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

package ai.certifai.server;

import ai.certifai.config.PortSelector;
import ai.certifai.database.loader.LoaderStatus;
import ai.certifai.database.loader.ProjectLoader;
import ai.certifai.database.portfolio.PortfolioSQLQuery;
import ai.certifai.database.project.ProjectSQLQuery;
import ai.certifai.selector.FileSelector;
import ai.certifai.selector.FolderSelector;
import ai.certifai.selector.SelectorHandler;
import ai.certifai.selector.SelectorStatus;
import ai.certifai.util.ConversionHandler;
import ai.certifai.util.http.HTTPResponseHandler;
import ai.certifai.util.message.ReplyHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.StaticHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
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

    public ServerVerticle(){
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

    //PUT http://localhost:{port}/createproject/:projectname http://localhost:{port}/createproject/helloworld
    private void createProjectInPortfolio(RoutingContext context)
    {
        String projectName = context.request().getParam(ServerConfig.PROJECT_NAME_PARAM);
        JsonObject request = new JsonObject().put(ServerConfig.PROJECT_NAME_PARAM, projectName);

        context.request().bodyHandler(h -> {

            DeliveryOptions createOptions = new DeliveryOptions().addHeader(ServerConfig.ACTION_KEYWORD, PortfolioSQLQuery.CREATE_NEW_PROJECT);

            vertx.eventBus().request(PortfolioSQLQuery.QUEUE, request, createOptions, reply -> {

                if(reply.succeeded())
                {
                    JsonObject response = (JsonObject) reply.result().body();

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
            });
        });
    }

    //GET http://localhost:{port}/project/:projectname http://localhost:{port}/project/projectname
    private void getProject(RoutingContext context)
    {
        String projectName = context.request().getParam(ServerConfig.PROJECT_NAME_PARAM);

        JsonObject request = new JsonObject().put(ServerConfig.PROJECT_NAME_PARAM, projectName);

        DeliveryOptions options = new DeliveryOptions().addHeader(ServerConfig.ACTION_KEYWORD, PortfolioSQLQuery.GET_PROJECT_UUID_LIST);

        vertx.eventBus().request(PortfolioSQLQuery.QUEUE, request, options, reply ->
        {
            if(reply.succeeded())
            {
                JsonObject response = (JsonObject) reply.result().body();

                if(ReplyHandler.isReplyOk(response))
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
    }

    //PUT http://localhost:{port}/selectproject/:projectname http://localhost:{port}/selectproject/helloworld
    private void selectProject(RoutingContext context)
    {
        String projectName = context.request().getParam(ServerConfig.PROJECT_NAME_PARAM);

        ProjectLoader loader = SelectorHandler.getProjectLoader(projectName);

        if(!checkLoader(loader, context)) return;

        LoaderStatus loaderStatus = loader.getLoaderStatus();

        if(loaderStatus != LoaderStatus.LOADING)
        {
            JsonObject jsonObject = new JsonObject().put(ServerConfig.PROJECT_NAME_PARAM, projectName);

            //get uuid list for processing
            DeliveryOptions options = new DeliveryOptions().addHeader(ServerConfig.ACTION_KEYWORD, PortfolioSQLQuery.GET_PROJECT_UUID_LIST);

            vertx.eventBus().request(PortfolioSQLQuery.QUEUE, jsonObject, options, reply ->
            {
                if(reply.succeeded())
                {
                    JsonObject oriUUIDResponse = (JsonObject) reply.result().body();

                    if(ReplyHandler.isReplyOk(oriUUIDResponse))
                    {
                        JsonArray uuidListArray = oriUUIDResponse.getJsonArray(ServerConfig.UUID_LIST_PARAM);
                        JsonObject removalObject = jsonObject.put(ServerConfig.UUID_LIST_PARAM, uuidListArray);

                        DeliveryOptions removalOptions = new DeliveryOptions().addHeader(ServerConfig.ACTION_KEYWORD, ProjectSQLQuery.REMOVE_OBSOLETE_UUID_LIST);

                        //start checking uuid if it's path is still exist
                        vertx.eventBus().request(ProjectSQLQuery.QUEUE, removalObject, removalOptions, fetch ->
                        {
                            if(fetch.succeeded())
                            {
                                JsonObject removalResponse = (JsonObject) fetch.result().body();
                                Integer removalStatus = removalResponse.getInteger(ReplyHandler.getMessageKey());

                                if (removalStatus == LoaderStatus.LOADED.ordinal()) {
                                    //request on portfolio database
                                    List<Integer> uuidCheckedList = SelectorHandler.getProjectLoaderUUIDList(projectName);

                                    jsonObject.put(ServerConfig.UUID_LIST_PARAM, uuidCheckedList);

                                    DeliveryOptions updateOption = new DeliveryOptions().addHeader(ServerConfig.ACTION_KEYWORD, PortfolioSQLQuery.UPDATE_PROJECT);

                                    vertx.eventBus().request(PortfolioSQLQuery.QUEUE, jsonObject, updateOption, updateFetch -> {

                                        if (updateFetch.succeeded())
                                        {
                                            JsonObject updatePortfolio = (JsonObject) updateFetch.result().body();

                                            if (ReplyHandler.isReplyFailed(updatePortfolio))
                                            {
                                                log.error("Save updates to portfolio database failed");
                                            }
                                        }
                                        else
                                        {
                                            log.error("Remove obsolete uuid list failed");
                                        }

                                    });
                                }
                            }
                            else
                            {
                                log.error("Remove obsolete uuid item failed");
                            }
                        });
                    }
                    else
                    {
                        log.error("Remove obsolete list resulted in error");
                    }
                }
            });
        }
        HTTPResponseHandler.configureOK(context, ReplyHandler.getOkReply()); //FIXME: This is terrible
    }

    private boolean checkLoader(ProjectLoader loader, RoutingContext context)
    {
        if(loader == null)
        {
            JsonObject jsonObject = ReplyHandler.getFailedReply();
            jsonObject.put(ReplyHandler.getMessageKey(), "Project name did not exist");
            HTTPResponseHandler.configureBadRequest(context, jsonObject);
            return false;
        }

        return true;
    }

    //PUT http://localhost:{port}/selectproject/status/:projectname
    private void selectProjectStatus(RoutingContext context)
    {
        String projectName = context.request().getParam(ServerConfig.PROJECT_NAME_PARAM);

        ProjectLoader projectLoader = SelectorHandler.getProjectLoader(projectName);

        if(!checkLoader(projectLoader, context)) return;

        LoaderStatus loaderStatus = projectLoader.getLoaderStatus();

        if(loaderStatus == LoaderStatus.ERROR)
        {
            HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError("Loader status error. Something wrong."));
        }
        else if(loaderStatus == LoaderStatus.LOADING)
        {
            JsonObject jsonObject = ReplyHandler.getOkReply();
            jsonObject.put(ServerConfig.PROGRESS_METADATA, projectLoader.getProgress());

            HTTPResponseHandler.configureOK(context, jsonObject);
        }
        else if((loaderStatus == LoaderStatus.LOADED)  || (loaderStatus == LoaderStatus.EMPTY))
        {
            DeliveryOptions options = new DeliveryOptions().addHeader(ServerConfig.ACTION_KEYWORD, PortfolioSQLQuery.GET_UUID_LABEL_LIST);

            vertx.eventBus().request(PortfolioSQLQuery.QUEUE, new JsonObject().put(ServerConfig.PROJECT_NAME_PARAM, projectName), options, reply ->
            {
                if (reply.succeeded()) {

                    JsonObject response = (JsonObject) reply.result().body();

                    if(ReplyHandler.isReplyOk(response))
                    {
                        response.put(ReplyHandler.getMessageKey(), LoaderStatus.LOADED.ordinal());

                        HTTPResponseHandler.configureOK(context, response);
                    }
                    else
                    {
                        //soft fail
                        HTTPResponseHandler.configureOK(context, response);
                        //HTTPResponseHandler.configureBadRequest(context, response);
                    }

                } else {
                    //soft fail
                    HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError("Failed to get reply for uuid label list"));
                    //HTTPResponseHandler.configureInternalServerError(context);
                }
            });
        }
        else if(loaderStatus == LoaderStatus.DID_NOT_INITIATED)
        {
            JsonObject object = new JsonObject();
            object.put(ReplyHandler.getMessageKey(), LoaderStatus.DID_NOT_INITIATED.ordinal());

            HTTPResponseHandler.configureOK(context, object);
        }
    }

    private void updateLabelInPortfolio(RoutingContext context)
    {
        String projectName = context.request().getParam(ServerConfig.PROJECT_NAME_PARAM);

        context.request().bodyHandler(h ->
        {
            io.vertx.core.json.JsonObject jsonObject = ConversionHandler.json2JSONObject(h.toJson());

            jsonObject.put(ServerConfig.PROJECT_NAME_PARAM, projectName);

            DeliveryOptions options = new DeliveryOptions().addHeader(ServerConfig.ACTION_KEYWORD, PortfolioSQLQuery.UPDATE_LABEL);

            vertx.eventBus().request(PortfolioSQLQuery.QUEUE, jsonObject, options, reply ->
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

    //GET http://localhost:{port}/projects
    private void getAllProjectNamesInPortfolio(RoutingContext context)
    {
        DeliveryOptions options = new DeliveryOptions().addHeader(ServerConfig.ACTION_KEYWORD, PortfolioSQLQuery.GET_ALL_PROJECTS);

        vertx.eventBus().request(PortfolioSQLQuery.QUEUE, new JsonObject(), options, reply -> {

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
                    //HTTPResponseHandler.configureBadRequest(context, response);
                }
            }
            else
            {
                HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError("Warning: Failed in getting all the projects"));
            }
        });

    }

    //GET http://localhost:{port}/select?projectname={projectname}&filetype={file/folder}
    private void selectFileType(RoutingContext context)
    {
        if(SelectorHandler.isDatabaseUpdating())
        {
            JsonObject jsonObject = new JsonObject();
            jsonObject.put(ReplyHandler.getMessageKey(), 2);
            jsonObject.put(ReplyHandler.getErrorMesageKey(), "Database is updating");

            //softfail
            //HTTPResponseHandler.configureMethodsNotAllowed(context, jsonObject);
            HTTPResponseHandler.configureOK(context, jsonObject);
        }
        else if (SelectorHandler.isWindowOpen())
        {
            JsonObject jsonObject = new JsonObject();
            jsonObject.put(ReplyHandler.getMessageKey(), 2);
            jsonObject.put(ReplyHandler.getErrorMesageKey(), "Window is open");

            //softfail
            //HTTPResponseHandler.configureMethodsNotAllowed(context, jsonObject);
            HTTPResponseHandler.configureOK(context, jsonObject);
        }
        else
        {
            String fileType = context.request().getParam(ServerConfig.FILETYPE_PARAM);
            String projectName = context.request().getParam(ServerConfig.PROJECT_NAME_PARAM);

            if(SelectorHandler.isProjectNameRegistered(projectName) == false)
            {
                //HTTPResponseHandler.configureBadRequest(context, ReplyHandler.reportProjectNameError());
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

                    //HTTPResponseHandler.configureBadRequest(context, jsonObject);
                    HTTPResponseHandler.configureOK(context, jsonObject);
                }
                else
                {
                    //set uuid generator
                    DeliveryOptions options = new DeliveryOptions().addHeader(ServerConfig.ACTION_KEYWORD, PortfolioSQLQuery.GET_PROJECT_UUID_LIST);
                    vertx.eventBus().request(PortfolioSQLQuery.QUEUE, new JsonObject().put(ServerConfig.PROJECT_NAME_PARAM, projectName), options, reply ->
                    {
                        if(reply.succeeded())
                        {
                            JsonObject response = (JsonObject) reply.result().body();

                            if(ReplyHandler.isReplyOk(response))
                            {
                                List<Integer> uuidList = ConversionHandler.jsonArray2IntegerList(response.getJsonArray(ServerConfig.UUID_LIST_PARAM));
                                //this is to initiate the generator id to the end of existing list
                                SelectorHandler.configureUUIDGenerator(uuidList);
                                SelectorHandler.setWindowState(true);

                                if (fileType.equals(SelectorHandler.FILE))
                                {
                                    fileSelector.runFileSelector();
                                }
                                else if (fileType.equals(SelectorHandler.FOLDER))
                                {
                                    folderSelector.runFolderSelector();
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

    //GET http://localhost:{port}/selectstatus/:projectname
    public void selectStatus(RoutingContext context) {

        String projectName = context.request().getParam(ServerConfig.PROJECT_NAME_PARAM);

        //check project name if exist
        if(SelectorHandler.isProjectNameRegistered(projectName) == false)
        {
            //HTTPResponseHandler.configureBadRequest(context, ReplyHandler.reportProjectNameError());

            JsonObject object = new JsonObject();
            object.put(ReplyHandler.getMessageKey(), SelectorStatus.ERROR);
            object.put(ReplyHandler.getErrorMesageKey(), "Project name did not exist");
            HTTPResponseHandler.configureOK(context, object);
        }
        else
        {
            if(SelectorHandler.isWindowOpen())
            {
                HTTPResponseHandler.configureOK(context, new JsonObject().put(ReplyHandler.getMessageKey(), SelectorStatus.WINDOW_OPEN));
            }
            else if (SelectorHandler.isDatabaseUpdating())
            {
                JsonObject res = new JsonObject();

                List metadata = new ArrayList<>(Arrays.asList(SelectorHandler.getCurrentProcessingUUID(), SelectorHandler.getUUIDListSize()));
                res.put(ServerConfig.PROGRESS_METADATA, metadata);
                res.put(ReplyHandler.getMessageKey(), SelectorStatus.WINDOW_CLOSE_UUID_CREATING);

                HTTPResponseHandler.configureOK(context, res);
            }
            else {
                JsonObject request = new JsonObject().put(ServerConfig.PROJECT_NAME_PARAM, projectName);

                DeliveryOptions options = new DeliveryOptions().addHeader(ServerConfig.ACTION_KEYWORD, PortfolioSQLQuery.GET_THUMBNAIL_LIST);

                vertx.eventBus().request(PortfolioSQLQuery.QUEUE, request, options, reply ->
                {
                    if(reply.succeeded())
                    {
                        JsonObject response = (JsonObject) reply.result().body();

                        if(ReplyHandler.isReplyOk(response))
                        {
                            List<Integer> intList = ConversionHandler.jsonArray2IntegerList(response.getJsonArray(ServerConfig.UUID_LIST_PARAM));

                            if(intList.isEmpty())
                            {
                                response.put(ReplyHandler.getMessageKey(), SelectorStatus.WINDOW_CLOSE_UUID_NOT_CREATED);
                                HTTPResponseHandler.configureOK(context, response);
                            }
                            else
                            {
                                response.put(ReplyHandler.getMessageKey(), SelectorStatus.WINDOW_CLOSE_UUID_CREATED);
                                HTTPResponseHandler.configureOK(context, response);
                            }
                        }
                        else
                        {
                            //temporary fix to fit this function
                            response.put(ReplyHandler.getMessageKey(), SelectorStatus.ERROR);
                            HTTPResponseHandler.configureOK(context, response);
                        }
                    }
                    else
                    {
                        JsonObject object = new JsonObject();

                        object.put(ReplyHandler.getMessageKey(), SelectorStatus.ERROR);
                        object.put(ReplyHandler.getErrorMesageKey(), "Failed in getting thumbnail list");

                        HTTPResponseHandler.configureOK(context, object);
                    }
                });
            }
        }
    }


    private void updateData(RoutingContext context) {
        String projectName = context.request().getParam(ServerConfig.PROJECT_NAME_PARAM);
        Integer uuid = Integer.parseInt(context.request().getParam(ServerConfig.UUID_PARAM));

        context.request().bodyHandler(h ->
        {
            JsonObject request = new JsonObject().put(ServerConfig.UUID_PARAM, uuid)
                    .put(ServerConfig.PROJECT_NAME_PARAM, projectName);

            DeliveryOptions updateOptions = new DeliveryOptions().addHeader(ServerConfig.ACTION_KEYWORD, ProjectSQLQuery.UPDATE_DATA);

            io.vertx.core.json.JsonObject jsonObject = ConversionHandler.json2JSONObject(h.toJson());

            vertx.eventBus().request(ProjectSQLQuery.QUEUE, jsonObject, updateOptions, fetch ->
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

    //GET http://localhost:{port}/thumbnail?projectname=helloworld&uuid=1234
    public void getThumbnailWithMetadata(RoutingContext context)
    {
        String projectName = context.request().getParam(ServerConfig.PROJECT_NAME_PARAM);
        Integer uuid = Integer.parseInt(context.request().getParam(ServerConfig.UUID_PARAM));

        JsonObject request = new JsonObject().put(ServerConfig.UUID_PARAM, uuid)
                                             .put(ServerConfig.PROJECT_NAME_PARAM, projectName);

        DeliveryOptions thumbnailOptions = new DeliveryOptions().addHeader(ServerConfig.ACTION_KEYWORD, ProjectSQLQuery.RETRIEVE_DATA);

        vertx.eventBus().request(ProjectSQLQuery.QUEUE, request, thumbnailOptions, fetch -> {

            if (fetch.succeeded())
            {
                JsonObject result = (JsonObject) fetch.result().body();

                if(ReplyHandler.isReplyOk(result))
                {
                    HTTPResponseHandler.configureOK(context, result);

                }
                else {
                    //soft fail
                    HTTPResponseHandler.configureOK(context, result);
                    //HTTPResponseHandler.configureBadRequest(context, result);
                }

            }
            else
            {
                HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError("Warning: Failed in retrieving thumbnail"));
            }
        });
    }



    //GET http://localhost:{port}/imgsrc?projectname=helloworld&uuid=1234
    public void getImageSource(RoutingContext context) {
        String projectName = context.request().getParam(ServerConfig.PROJECT_NAME_PARAM);
        Integer uuid = Integer.parseInt(context.request().getParam(ServerConfig.UUID_PARAM));

        JsonObject request = new JsonObject().put(ServerConfig.UUID_PARAM, uuid)
                .put(ServerConfig.PROJECT_NAME_PARAM, projectName);


        DeliveryOptions imgSrcOptions = new DeliveryOptions().addHeader(ServerConfig.ACTION_KEYWORD, ProjectSQLQuery.RETRIEVE_DATA_PATH);

        vertx.eventBus().request(ProjectSQLQuery.QUEUE, request, imgSrcOptions, fetch -> {

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


    @Override
    public void start(Promise<Void> promise)
    {
        Router router = Router.router(vertx);

        //display for content in webroot
        router.route().handler(StaticHandler.create());

        router.get("/selectproject/:projectname").handler(this::selectProject);
        router.get("/selectproject/status/:projectname").handler(this::selectProjectStatus);

        router.get("/select").handler(this::selectFileType);
        router.get("/selectstatus/:projectname").handler(this::selectStatus);

        router.put("/createproject/:projectname").handler(this::createProjectInPortfolio);

        router.put("/updatelabel/:projectname").handler(this::updateLabelInPortfolio);

        router.get("/project/:projectname").handler(this::getProject);

        router.get("/projects").handler(this::getAllProjectNamesInPortfolio);
        router.get("/thumbnail").handler(this::getThumbnailWithMetadata);

        router.get("/imgsrc").handler(this::getImageSource);
        router.put("/update").handler(this::updateData);

        vertx.createHttpServer()
                .requestHandler(router)
                .exceptionHandler(Throwable::printStackTrace)
                .listen(PortSelector.getHostingPort(), r -> {
                    if (r.succeeded()) {
                        //log.info("HTTPServer start successfully");
                        promise.complete();
                    } else {

                        log.debug("HTTPServer failed to start");
                        promise.fail(r.cause());
                    }
                });
    }
}
