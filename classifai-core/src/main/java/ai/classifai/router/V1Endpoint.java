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

import ai.classifai.database.portfolio.PortfolioDB;
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

        portfolioDB.getProjectMetadata(loader.getProjectId())
                .onSuccess(result -> HTTPResponseHandler.configureOK(context,
                        ReplyHandler.getOkReply().put(ParamConfig.getContent(), result)))
                .onFailure(cause -> HTTPResponseHandler.configureOK(context,
                        ReplyHandler.reportUserDefinedError("Failed to retrieve metadata for project " + projectName)));
    }

    /**
     * Get metadata of all projects
     * GET http://localhost:{port}/:annotation_type/projects/meta
     *
     */
    public void getAllProjectsMeta(RoutingContext context)
    {
        AnnotationType type = AnnotationHandler.getTypeFromEndpoint(context.request().getParam(ParamConfig.getAnnotationTypeParam()));

        portfolioDB.getAllProjectsMeta(type.ordinal())
                .onSuccess(result -> HTTPResponseHandler.configureOK(context,
                        ReplyHandler.getOkReply().put(ParamConfig.getContent(), result)))
                .onFailure(cause -> HTTPResponseHandler.configureOK(context,
                        ReplyHandler.reportUserDefinedError("Failure in getting all the projects for " + type.name())));
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

        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());

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
                Future<Void> future = portfolioDB.loadProject(loader.getProjectId());
                ReplyHandler.sendEmptyResult(context, future,
                        "Failed to load project " + projectName + ". Check validity of data points failed.");
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

        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());
        String projectID = ProjectHandler.getProjectId(projectName, type.ordinal());
        String uuid = context.request().getParam(ParamConfig.getUuidParam());

        portfolioDB.getThumbnail(projectID, uuid)
                .onSuccess(result -> HTTPResponseHandler.configureOK(context, result))
                .onFailure(cause -> HTTPResponseHandler.configureOK(context,
                        ReplyHandler.reportUserDefinedError("Fail retrieving thumbnail")));
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

        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());
        String projectID = ProjectHandler.getProjectId(projectName, type.ordinal());
        String uuid = context.request().getParam(ParamConfig.getUuidParam());

        portfolioDB.getImageSource(projectID, uuid, projectName)
                .onSuccess(result -> HTTPResponseHandler.configureOK(context,
                        ReplyHandler.getOkReply().put(ParamConfig.getImgSrcParam(), result)))
                .onFailure(cause -> HTTPResponseHandler.configureOK(context,
                        ReplyHandler.reportUserDefinedError("Fail getting image source")));
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

        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());

        String projectID = ProjectHandler.getProjectId(projectName, type.ordinal());

        ProjectLoader loader = Objects.requireNonNull(ProjectHandler.getProjectLoader(projectID));

        if(helper.checkIfProjectNull(context, projectID, projectName)) return;

        context.request().bodyHandler(handler -> {
            JsonObject requestBody = handler.toJsonObject();

            portfolioDB.updateData(requestBody, projectID)
                    .onSuccess(result -> {
                        updateLastModifiedDate(loader);
                        HTTPResponseHandler.configureOK(context);
                    })
                    .onFailure(cause -> HTTPResponseHandler.configureOK(context,
                            ReplyHandler.reportUserDefinedError("Failure in updating database for " + type + " project: " + projectName)));
        });
    }

    private void updateLastModifiedDate(ProjectLoader loader)
    {
        String projectID = loader.getProjectId();

        Version version = loader.getProjectVersion().getCurrentVersion();

        version.setLastModifiedDate(new DateTime());

        portfolioDB.updateLastModifiedDate(projectID, version.getDbFormat())
                .onFailure(cause -> log.info("Databse update fail. Type: " + loader.getAnnotationType() + " Project: " + loader.getProjectName()));
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

            Future<Void> future = portfolioDB.updateLabels(projectID, requestBody);
            ReplyHandler.sendEmptyResult(context, future, "Fail to update labels: " + projectName);
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

        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());

        String projectID = ProjectHandler.getProjectId(projectName, type.ordinal());

        if(helper.checkIfProjectNull(context, projectID, projectName)) return;

        String errorMessage = "Failure in delete project name: " + projectName + " for " + type.name();


        portfolioDB.deleteProjectFromPortfolioDb(projectID)
                .compose(result -> portfolioDB.deleteProjectFromAnnotationDb(projectID))
                .onSuccess(result -> {
                    ProjectHandler.deleteProjectFromCache(projectID);
                    HTTPResponseHandler.configureOK(context);
                })
                .onFailure(cause -> HTTPResponseHandler.configureOK(context,
                        ReplyHandler.reportUserDefinedError(errorMessage)));
    }

}
