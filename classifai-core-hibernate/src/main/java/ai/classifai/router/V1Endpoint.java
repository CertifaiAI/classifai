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
import ai.classifai.util.ParamConfig;
import ai.classifai.util.http.HTTPResponseHandler;
import ai.classifai.util.message.ReplyHandler;
import ai.classifai.util.type.AnnotationType;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;


/**
 * Classifai v1 endpoints
 *
 * @author devenyantis
 */
@Slf4j
public class V1Endpoint extends EndpointBase
{
    /**
     * Retrieve specific project metadata
     *
     * GET http://localhost:{port}/:annotation_type/projects/:project_name/meta
     *
     */
    public void getProjectMetadata(RoutingContext context)
    {
        JsonObject request = paramHandler.projectParamToJson(context);

        DeliveryOptions options = getDeliveryOptions(DbActionConfig.GET_PROJECT_META);

        vertx.eventBus().request(DbActionConfig.QUEUE, request, options)
                .onSuccess(msg -> sendResponseBody(msg, context))
                .onFailure(throwable -> HTTPResponseHandler.configureOK(context,
                        ReplyHandler.reportUserDefinedError("Failed to retrieve metadata")));
    }

    /**
     * Get metadata of all projects
     * GET http://localhost:{port}/:annotation_type/projects/meta
     *
     */
    public void getAllProjectsMeta(RoutingContext context)
    {
        JsonObject request = paramHandler.annoParamToJson(context);

        DeliveryOptions options = getDeliveryOptions(DbActionConfig.GET_ALL_PROJECT_META);

        vertx.eventBus().request(DbActionConfig.QUEUE, request, options)
                .onSuccess(msg -> sendResponseBody(msg, context))
                .onFailure(throwable -> HTTPResponseHandler.configureOK(context,
                        ReplyHandler.reportUserDefinedError("Failure in getting all the projects")));
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
        JsonObject request = paramHandler.projectParamToJson(context);

        DeliveryOptions options = getDeliveryOptions(DbActionConfig.LOAD_PROJECT);

        vertx.eventBus().request(DbActionConfig.QUEUE, request, options)
                .onSuccess(msg -> sendResponseBody(msg, context))
                .onFailure(throwable -> HTTPResponseHandler.configureOK(context,
                        ReplyHandler.reportUserDefinedError("Fail to load project")));
    }


    /**
     * Get status of loading a project
     *
     * GET http://localhost:{port}/:annotation_type/projects/:project_name/loadingstatus
     *
     * Example:
     * GET http://localhost:{port}/seg/projects/helloworld/loadingstatus
     */
    // FIXME: To be deleted
    public void loadProjectStatus(RoutingContext context)
    {
        JsonObject request = paramHandler.projectParamToJson(context);

        DeliveryOptions options = getDeliveryOptions(DbActionConfig.LOAD_PROJECT);

        vertx.eventBus().request(DbActionConfig.QUEUE, request, options, reply -> {

            if (reply.succeeded())
            {
                JsonObject response = (JsonObject) reply.result().body();
                response.put("message", 2); // FIXME: hardcoded

                HTTPResponseHandler.configureOK(context, response);
            }
            else
            {
                HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError("Fail to load project"));
            }
        });
    }

    /**
     * Retrieve thumbnail with metadata
     *
     * GET http://localhost:{port}/:annotation_type/projects/:project_name/uuid/:uuid/thumbnail
     *
     */
    public void getThumbnail(RoutingContext context)
    {
        JsonObject request = paramHandler.dataParamToJson(context);

        DeliveryOptions options = getDeliveryOptions(DbActionConfig.GET_THUMBNAIL);

        vertx.eventBus().request(DbActionConfig.QUEUE, request, options)
                .onSuccess(msg -> sendResponseBody(msg, context))
                .onFailure(throwable -> HTTPResponseHandler.configureOK(context,
                        ReplyHandler.reportUserDefinedError("Failure in retrieving thumbnail")));
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
        JsonObject request = paramHandler.dataParamToJson(context);

        DeliveryOptions options = getDeliveryOptions(DbActionConfig.GET_IMAGE_SOURCE);

        vertx.eventBus().request(DbActionConfig.QUEUE, request, options)
                .onSuccess(msg -> sendResponseBody(msg, context))
                .onFailure(throwable -> HTTPResponseHandler.configureOK(context,
                        ReplyHandler.reportUserDefinedError("Failure in retrieving image source")));
    }
//
//    /***
//     *
//     * Update labelling information
//     *
//     * PUT http://localhost:{port}/:annotation_type/projects/:project_name/uuid/:uuid/update
//     *
//     */
//    public void updateData(RoutingContext context)
//    {
//        AnnotationType type = AnnotationHandler.getTypeFromEndpoint(context.request().getParam(ParamConfig.getAnnotationTypeParam()));
//        String queue = helper.getDbQuery(type);
//
//        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());
//
//        String projectID = ProjectHandler.getProjectId(projectName, type.ordinal());
//
//        if(helper.checkIfProjectNull(context, projectID, projectName)) return;
//
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
//                        JsonObject response = (JsonObject) fetch.result().body();
//
//                        HTTPResponseHandler.configureOK(context, response);
//
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
//    }
//
//
//    /***
//     *
//     * Update labels
//     *
//     * PUT http://localhost:{port}/:annotation_type/projects/:project_name/newlabels
//     *
//     */
//    public void updateLabels(RoutingContext context)
//    {
//        AnnotationType type = AnnotationHandler.getTypeFromEndpoint(context.request().getParam(ParamConfig.getAnnotationTypeParam()));
//
//        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());
//
//        String projectID = ProjectHandler.getProjectId(projectName, type.ordinal());
//
//        if(helper.checkIfProjectNull(context, projectID, projectName)) return;
//
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
//    }

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
        JsonObject request = paramHandler.projectParamToJson(context);

        DeliveryOptions options = getDeliveryOptions(DbActionConfig.DELETE_PROJECT);

        vertx.eventBus().request(DbActionConfig.QUEUE, request, options)
                .onSuccess(msg -> sendResponseBody(msg, context))
                .onFailure(throwable -> HTTPResponseHandler.configureOK(context,
                        ReplyHandler.reportUserDefinedError("Failure in deleting project")));
    }
//    {
//        AnnotationType type = AnnotationHandler.getTypeFromEndpoint(context.request().getParam(ParamConfig.getAnnotationTypeParam()));
//
//        String queue = helper.getDbQuery(type);
//
//        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());
//
//        String projectID = ProjectHandler.getProjectId(projectName, type.ordinal());
//
//        if(helper.checkIfProjectNull(context, projectID, projectName)) return;
//
//        JsonObject request = new JsonObject()
//                .put(ParamConfig.getProjectIdParam(), projectID);
//
//        DeliveryOptions options = new DeliveryOptions().addHeader(ParamConfig.getActionKeyword(), PortfolioDbQuery.getDeleteProject());
//
//        String errorMessage = "Failure in delete project name: " + projectName + " for " + type.name();
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
//    }

}
