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
import ai.classifai.database.portfolio.PortfolioDB;
import ai.classifai.database.portfolio.PortfolioDbQuery;
import ai.classifai.database.versioning.Version;
import ai.classifai.loader.ProjectLoader;
import ai.classifai.loader.ProjectLoaderStatus;
import ai.classifai.util.ParamConfig;
import ai.classifai.util.datetime.DateTime;
import ai.classifai.util.http.HTTPResponseHandler;
import ai.classifai.util.message.ReplyHandler;
import ai.classifai.util.project.ProjectHandler;
import ai.classifai.util.type.AnnotationHandler;
import ai.classifai.util.type.AnnotationType;
import io.vertx.core.Future;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.Objects;

/**
 * Classifai v1 endpoints
 *
 * @author devenyantis
 */
@Slf4j
public class V1Endpoint extends EndpointBase
{
    @Setter
    private PortfolioDB portfolioDB;

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

        ProjectLoader loader = Objects.requireNonNull(ProjectHandler.getProjectLoader(projectName, type));

        if(helper.checkIfProjectNull(context, loader, projectName)) return;


        Future<JsonObject> future = portfolioDB.getProjectMetadata(loader.getProjectId());
        future.onComplete(result -> {
           if(result.succeeded()) {
               HTTPResponseHandler.configureOK(context, future.result());
           } else {
               HTTPResponseHandler.configureOK(context,
                       ReplyHandler.reportUserDefinedError("Failed to retrieve metadata for project " + projectName));
           }
        });


//        JsonObject jsonObject = new JsonObject().put(ParamConfig.getProjectIdParam(), Objects.requireNonNull(loader).getProjectId());
//
//        //load label list
//        DeliveryOptions metadataOptions = new DeliveryOptions().addHeader(ParamConfig.getActionKeyword(), PortfolioDbQuery.getRetrieveProjectMetadata());
//
//        vertx.eventBus().request(PortfolioDbQuery.getQueue(), jsonObject, metadataOptions, metaReply ->
//        {
//            if (metaReply.succeeded()) {
//
//                JsonObject metaResponse = (JsonObject) metaReply.result().body();
//
//                if (ReplyHandler.isReplyOk(metaResponse))
//                {
//                    HTTPResponseHandler.configureOK(context, metaResponse);
//                }
//                else
//                {
//                    HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError("Failed to retrieve metadata for project " + projectName));
//                }
//            }
//            else
//            {
//                HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError("Server reply failure message when retrieving project metadata " + projectName));
//            }
//        });
    }

    /**
     * Get metadata of all projects
     * GET http://localhost:{port}/:annotation_type/projects/meta
     *
     */
    public void getAllProjectsMeta(RoutingContext context)
    {
        AnnotationType type = AnnotationHandler.getTypeFromEndpoint(context.request().getParam(ParamConfig.getAnnotationTypeParam()));

        Future<JsonObject> future = portfolioDB.getAllProjectsMeta(type.ordinal());
        future.onComplete(result -> {
           if(result.succeeded()) {
               HTTPResponseHandler.configureOK(context, future.result());
           } else {
               HTTPResponseHandler.configureOK(context,
                       ReplyHandler.reportUserDefinedError("Failure in getting all the projects for " + type.name()));
           }
        });


//        JsonObject request = new JsonObject()
//                .put(ParamConfig.getAnnotationTypeParam(), type.ordinal());
//
//        DeliveryOptions options = new DeliveryOptions().addHeader(ParamConfig.getActionKeyword(), PortfolioDbQuery.getRetrieveAllProjectsMetadata());
//
//        vertx.eventBus().request(PortfolioDbQuery.getQueue(), request, options, reply -> {
//
//            if(reply.succeeded())
//            {
//                JsonObject response = (JsonObject) reply.result().body();
//
//                HTTPResponseHandler.configureOK(context, response);
//            }
//            else
//            {
//                HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError("Failure in getting all the projects for " + type.name()));
//            }
//        });
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

//        String queue = helper.getDbQuery(type);

        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());

        log.info("Load project: " + projectName + " of annotation type: " + type.name());

        ProjectLoader loader = ProjectHandler.getProjectLoader(projectName, type);

        if(helper.checkIfProjectNull(context, loader, projectName)) return;

        loader.toggleFrontEndLoaderParam(); //if project is_new = true, change to false since loading the project

        if(loader.isCloud())
        {
            //FIXME
            HTTPResponseHandler.configureOK(context);
        }
        else
        {
            ProjectLoaderStatus projectLoaderStatus = loader.getProjectLoaderStatus();

            //Project exist, did not load in ProjectLoader, proceed with loading and checking validity of uuid from database
            if(projectLoaderStatus.equals(ProjectLoaderStatus.DID_NOT_INITIATED) || projectLoaderStatus.equals(ProjectLoaderStatus.LOADED))
            {
                loader.setProjectLoaderStatus(ProjectLoaderStatus.LOADING);


                Future<JsonObject> future = portfolioDB.loadProject(loader.getProjectId(), helper.getDbQuery(type));
                future.onComplete(result -> {
                    if(result.succeeded()) {
                        HTTPResponseHandler.configureOK(context);
                    } else {
                        HTTPResponseHandler.configureOK(context,
                                ReplyHandler.reportUserDefinedError("Failed to load project " + projectName + ". Check validity of data points failed."));
                    }
                });


//                JsonObject jsonObject = new JsonObject().put(ParamConfig.getProjectIdParam(), loader.getProjectId());
//
//                DeliveryOptions uuidListOptions = new DeliveryOptions().addHeader(ParamConfig.getActionKeyword(), AnnotationQuery.getLoadValidProjectUuid());
//
//                //start checking uuid if it's path is still exist
//                vertx.eventBus().request(queue, jsonObject, uuidListOptions, fetch ->
//                {
//                    JsonObject removalResponse = (JsonObject) fetch.result().body();
//
//                    if (ReplyHandler.isReplyOk(removalResponse))
//                    {
//                        HTTPResponseHandler.configureOK(context);
//                    }
//                    else
//                    {
//                        HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError("Failed to load project " + projectName + ". Check validity of data points failed."));
//                    }
//                });

            }
            else if(projectLoaderStatus.equals(ProjectLoaderStatus.LOADING))
            {
                HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError("Loading project is in progress in the backend. Did not reinitiated."));
            }
            else if(projectLoaderStatus.equals(ProjectLoaderStatus.ERROR))
            {
                HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError("LoaderStatus with error message when loading project " + projectName + ".Loading project aborted. "));
            }
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

        if (helper.checkIfProjectNull(context, projectLoader, projectName)) return;

        ProjectLoaderStatus projectLoaderStatus = projectLoader.getProjectLoaderStatus();

        if (projectLoaderStatus.equals(ProjectLoaderStatus.LOADING))
        {
            JsonObject jsonObject = new JsonObject();
            jsonObject.put(ReplyHandler.getMessageKey(), projectLoaderStatus.ordinal());

            jsonObject.put(ParamConfig.getProgressMetadata(), projectLoader.getProgress());

            HTTPResponseHandler.configureOK(context, jsonObject);

        } else if (projectLoaderStatus.equals(ProjectLoaderStatus.LOADED)) {

            JsonObject jsonObject = new JsonObject();
            jsonObject.put(ReplyHandler.getMessageKey(), projectLoaderStatus.ordinal());

            // Remove empty string from label list
            projectLoader.getLabelList().removeAll(Collections.singletonList(""));

            jsonObject.put(ParamConfig.getLabelListParam(), projectLoader.getLabelList());
            jsonObject.put(ParamConfig.getUuidListParam(), projectLoader.getSanityUuidList());

            HTTPResponseHandler.configureOK(context, jsonObject);

        }
        else if (projectLoaderStatus.equals(ProjectLoaderStatus.DID_NOT_INITIATED) || projectLoaderStatus.equals(ProjectLoaderStatus.ERROR))
        {
            JsonObject jsonObject = new JsonObject();
            jsonObject.put(ReplyHandler.getMessageKey(), ProjectLoaderStatus.ERROR.ordinal());
            jsonObject.put(ReplyHandler.getErrorMesageKey(), "Loading failed. LoaderStatus error for project " + projectName);

            HTTPResponseHandler.configureOK(context, jsonObject);
        }
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

        String queue = helper.getDbQuery(type);

        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());
        String projectID = ProjectHandler.getProjectId(projectName, type.ordinal());
        String uuid = context.request().getParam(ParamConfig.getUuidParam());


        Future<JsonObject> future = portfolioDB.getThumbnail(projectID, helper.getDbQuery(type), uuid);
        future.onComplete(result -> {
            if(result.succeeded()) {
                HTTPResponseHandler.configureOK(context, future.result());
            } else {
                HTTPResponseHandler.configureOK(context,
                        ReplyHandler.reportUserDefinedError("Failure in retrieving thumbnail: " + result.cause().getMessage()));
            }
        });


//        JsonObject request = new JsonObject()
//                .put(ParamConfig.getUuidParam(), uuid)
//                .put(ParamConfig.getProjectIdParam(), projectID);
//
//        DeliveryOptions thumbnailOptions = new DeliveryOptions().addHeader(ParamConfig.getActionKeyword(), AnnotationQuery.getQueryData());
//
//        vertx.eventBus().request(queue, request, thumbnailOptions, fetch -> {
//
//            if (fetch.succeeded())
//            {
//                JsonObject result = (JsonObject) fetch.result().body();
//
//                HTTPResponseHandler.configureOK(context, result);
//
//            }
//            else
//            {
//                HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError("Failure in retrieving thumbnail: " + fetch.cause().getMessage()));
//            }
//        });
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

        String queue = helper.getDbQuery(type);

        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());
        String projectID = ProjectHandler.getProjectId(projectName, type.ordinal());
        String uuid = context.request().getParam(ParamConfig.getUuidParam());


        Future<JsonObject> future = portfolioDB.getImageSource(projectID, helper.getDbQuery(type), uuid, projectName);
        future.onComplete(result -> {
           if(result.succeeded()) {
               HTTPResponseHandler.configureOK(context, future.result());
           } else {
               HTTPResponseHandler.configureOK(context,
                       ReplyHandler.reportUserDefinedError("Failure in getting image source."));
           }
        });


//        JsonObject request = new JsonObject()
//                .put(ParamConfig.getUuidParam(), uuid)
//                .put(ParamConfig.getProjectIdParam(), projectID)
//                .put(ParamConfig.getProjectNameParam(), projectName);
//
//        DeliveryOptions imgSrcOptions = new DeliveryOptions().addHeader(ParamConfig.getActionKeyword(), AnnotationQuery.getRetrieveDataPath());
//
//        vertx.eventBus().request(queue, request, imgSrcOptions, fetch -> {
//
//            if (fetch.succeeded()) {
//
//                JsonObject result = (JsonObject) fetch.result().body();
//
//                HTTPResponseHandler.configureOK(context, result);
//
//            }
//            else {
//                HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError("Failure in getting image source."));
//            }
//        });
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
        String queue = helper.getDbQuery(type);

        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());

        String projectID = ProjectHandler.getProjectId(projectName, type.ordinal());

        ProjectLoader loader = Objects.requireNonNull(ProjectHandler.getProjectLoader(projectID));

        if(helper.checkIfProjectNull(context, projectID, projectName)) return;


        context.request().bodyHandler(handler -> {
            JsonObject requestBody = handler.toJsonObject();

            Future<JsonObject> future = portfolioDB.updateData(projectID, helper.getDbQuery(type), requestBody);
            future.onComplete(result -> {
                if(result.succeeded()) {
                    updateLastModifiedDate(loader, context);
                } else {
                    HTTPResponseHandler.configureOK(context,
                            ReplyHandler.reportUserDefinedError("Failure in updating database for " + type + " project: " + projectName));
                }
            });

        });



//        context.request().bodyHandler(h ->
//        {
//            DeliveryOptions updateOptions = new DeliveryOptions().addHeader(ParamConfig.getActionKeyword(), AnnotationQuery.getUpdateData());
//
//            try
//            {
//                JsonObject jsonObject = h.toJsonObject();
//
//                jsonObject.put(ParamConfig.getProjectIdParam(), projectID);
//
//                vertx.eventBus().request(queue, jsonObject, updateOptions, fetch ->
//                {
//                    if (fetch.succeeded())
//                    {
//                        updateLastModifiedDate(loader, context);
//                    }
//                    else
//                    {
//                        HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError("Failure in updating database for " + type + " project: " + projectName));
//                    }
//                });
//
//            }catch (Exception e)
//            {
//                HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError("Request payload failed to parse: " + projectName + ". " + e));
//            }
//        });
    }

    private void updateLastModifiedDate(ProjectLoader loader, RoutingContext context)
    {
        String queue = PortfolioDbQuery.getQueue();

        String projectID = loader.getProjectId();

        Version version = loader.getProjectVersion().getCurrentVersion();

        version.setLastModifiedDate(new DateTime());

        Future<JsonObject> future = portfolioDB.updateLastModifiedDate(projectID, version.getDbFormat());
        future.onComplete(result -> {
           if(result.succeeded()) {
               HTTPResponseHandler.configureOK(context, future.result());
           } else {
               HTTPResponseHandler.configureOK(context,
                       ReplyHandler.reportUserDefinedError("Failure in updating database for " + loader.getAnnotationType() + " project: " + loader.getProjectName()));
           }
        });

//        JsonObject jsonObj = new JsonObject();
//        jsonObj.put(ParamConfig.getProjectIdParam(), projectID);
//        jsonObj.put(ParamConfig.getCurrentVersionParam(), version.getDbFormat());
//
//        DeliveryOptions updateOptions = new DeliveryOptions().addHeader(ParamConfig.getActionKeyword(), PortfolioDbQuery.getUpdateLastModifiedDate());
//
//        vertx.eventBus().request(queue, jsonObj, updateOptions, fetch ->
//        {
//            if (fetch.succeeded())
//            {
//                JsonObject response = (JsonObject) fetch.result().body();
//
//                HTTPResponseHandler.configureOK(context, response);
//            }
//            else
//            {
//                HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError("Failure in updating database for " + loader.getAnnotationType() + " project: " + loader.getProjectName()));
//            }
//        });
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

        String projectID = ProjectHandler.getProjectId(projectName, type.ordinal());

        if(helper.checkIfProjectNull(context, projectID, projectName)) return;



        context.request().bodyHandler(handlers -> {
            JsonObject requestBody = handlers.toJsonObject();
            Future<JsonObject> future = portfolioDB.updateLabels(projectID, requestBody);
            future.onComplete(result -> {
                if(result.succeeded()) {
                    HTTPResponseHandler.configureOK(context, future.result());
                } else {
                    HTTPResponseHandler.configureOK(context,
                            ReplyHandler.reportUserDefinedError("Fail to update labels: " + projectName));
                }
            });
        });


//        context.request().bodyHandler(h ->
//        {
//            try
//            {
//                JsonObject jsonObject = h.toJsonObject();
//
//                jsonObject.put(ParamConfig.getProjectIdParam(), projectID);
//
//                DeliveryOptions options = new DeliveryOptions().addHeader(ParamConfig.getActionKeyword(), PortfolioDbQuery.getUpdateLabelList());
//
//                vertx.eventBus().request(PortfolioDbQuery.getQueue(), jsonObject, options, reply ->
//                {
//                    if (reply.succeeded()) {
//                        JsonObject response = (JsonObject) reply.result().body();
//
//                        HTTPResponseHandler.configureOK(context, response);
//                    }
//                });
//            }
//            catch (Exception e)
//            {
//                HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError("Request payload failed to parse: " + projectName + ". " + e));
//
//            }
//        });
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

        String queue = helper.getDbQuery(type);

        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());

        String projectID = ProjectHandler.getProjectId(projectName, type.ordinal());

        if(helper.checkIfProjectNull(context, projectID, projectName)) return;

        String errorMessage = "Failure in delete project name: " + projectName + " for " + type.name();

        Future<JsonObject> future = portfolioDB.deleteProjectFromPortfolioDb(projectID);
        future.onComplete(result -> {
            if(result.succeeded()) {
                Future<JsonObject> annotationFuture = portfolioDB.deleteProjectFromAnnotationDb(projectID, helper.getDbQuery(type));
                annotationFuture.onComplete(annotationResult -> {
                    if (annotationResult.succeeded()) {
                        //delete in Project Handler
                        ProjectHandler.deleteProjectFromCache(projectID);
                        HTTPResponseHandler.configureOK(context, future.result());
                    } else {
                        HTTPResponseHandler.configureOK(context,
                                ReplyHandler.reportUserDefinedError(errorMessage));
                    }
                });
            } else {
                HTTPResponseHandler.configureOK(context,
                        ReplyHandler.reportUserDefinedError(errorMessage));
            }
        });


//        JsonObject request = new JsonObject()
//                .put(ParamConfig.getProjectIdParam(), projectID);
//
//        DeliveryOptions options = new DeliveryOptions().addHeader(ParamConfig.getActionKeyword(), PortfolioDbQuery.getDeleteProject());
//
//        vertx.eventBus().request(PortfolioDbQuery.getQueue(), request, options, reply -> {
//
//            if(reply.succeeded())
//            {
//                JsonObject response = (JsonObject) reply.result().body();
//
//                if(ReplyHandler.isReplyOk(response)) {
//                    //delete in respective Table
//                    DeliveryOptions deleteListOptions = new DeliveryOptions().addHeader(ParamConfig.getActionKeyword(), AnnotationQuery.getDeleteProject());
//
//                    vertx.eventBus().request(queue, request, deleteListOptions, fetch -> {
//
//                        if (fetch.succeeded()) {
//                            JsonObject replyResponse = (JsonObject) fetch.result().body();
//
//                            //delete in Project Handler
//                            ProjectHandler.deleteProjectFromCache(projectID);
//                            HTTPResponseHandler.configureOK(context, replyResponse);
//                        } else {
//                            HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError(errorMessage));
//                        }
//                    });
//                }
//            }
//            else
//            {
//                HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError(errorMessage  + " from Portfolio Database"));
//            }
//        });
    }

}
