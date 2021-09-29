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
import ai.classifai.dto.api.reader.DataUpdateReader;
import ai.classifai.dto.api.reader.LabelListReader;
import ai.classifai.dto.api.reader.body.LabelListBody;
import ai.classifai.dto.api.response.ImageSourceResponse;
import ai.classifai.dto.api.response.LoadingStatusResponse;
import ai.classifai.dto.data.ThumbnailProperties;
import ai.classifai.loader.ProjectLoader;
import ai.classifai.loader.ProjectLoaderStatus;
import ai.classifai.util.datetime.DateTime;
import ai.classifai.util.http.ActionStatus;
import ai.classifai.util.http.HTTPResponseHandler;
import ai.classifai.util.message.ReplyHandler;
import ai.classifai.util.project.ProjectHandler;
import ai.classifai.util.type.AnnotationHandler;
import ai.classifai.util.type.AnnotationType;
import com.zandero.rest.annotation.RequestReader;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Collections;
import java.util.Objects;

/**
 * Classifai v1 endpoints
 *
 * @author devenyantis
 */
@Slf4j
@Path("/{annotation_type}/projects")
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
    @GET
    @Path("/{project_name}/meta")
    @Produces(MediaType.APPLICATION_JSON)
    public Future<ActionStatus> getProjectMetadata(@PathParam("annotation_type") String annotationType,
                                                   @PathParam("project_name") String projectName)
    {
        AnnotationType type = AnnotationHandler.getTypeFromEndpoint(annotationType);
        log.info("Get metadata of project: " + projectName + " of annotation type: " + type.name());

        ProjectLoader loader = Objects.requireNonNull(ProjectHandler.getProjectLoader(projectName, type));
        if(helper.checkIfProjectNull(loader)) {
            return HTTPResponseHandler.nullProjectResponse();
        }

        return portfolioDB.getProjectMetadata(loader.getProjectId())
                .map(ActionStatus::okWithResponse)
                .otherwise(cause -> ActionStatus.failedWithMessage("Failed to retrieve metadata for project " + projectName));
    }

    /**
     * Get metadata of all projects
     * GET http://localhost:{port}/:annotation_type/projects/meta
     *
     */
    @GET
    @Path("/meta")
    @Produces(MediaType.APPLICATION_JSON)
    public Future<ActionStatus> getAllProjectsMeta(@PathParam("annotation_type") String annotationType)
    {
        AnnotationType type = AnnotationHandler.getTypeFromEndpoint(annotationType);

        return portfolioDB.getAllProjectsMeta(type.ordinal())
                .map(ActionStatus::okWithResponse)
                .otherwise(cause -> ActionStatus.failedWithMessage("Failure in getting all the projects for " + type.name()));
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
    @GET
    @Path("/{project_name}")
    @Produces(MediaType.APPLICATION_JSON)
    public Future<ActionStatus> loadProject(@PathParam("annotation_type") String annotationType,
                                            @PathParam("project_name") String projectName)
    {
        AnnotationType type = AnnotationHandler.getTypeFromEndpoint(annotationType);

        ProjectLoader loader = ProjectHandler.getProjectLoader(projectName, type);
        Promise<ActionStatus> promise = Promise.promise();

        if(helper.checkIfProjectNull(loader)) {
            return HTTPResponseHandler.nullProjectResponse();
        }

        loader.toggleFrontEndLoaderParam(); //if project is_new = true, change to false since loading the project

        if(loader.isCloud())
        {
            //FIXME
            promise.complete(ActionStatus.ok());
        }
        else
        {
            ProjectLoaderStatus projectLoaderStatus = loader.getProjectLoaderStatus();

            //Project exist, did not load in ProjectLoader, proceed with loading and checking validity of uuid from database
            if(projectLoaderStatus.equals(ProjectLoaderStatus.DID_NOT_INITIATED) || projectLoaderStatus.equals(ProjectLoaderStatus.LOADED))
            {
                loader.setProjectLoaderStatus(ProjectLoaderStatus.LOADING);
                return portfolioDB.loadProject(loader.getProjectId())
                        .map(ActionStatus.ok())
                        .otherwise(ActionStatus.failedWithMessage("Failed to load project " + projectName + ". Check validity of data points failed."));
            }
            else if(projectLoaderStatus.equals(ProjectLoaderStatus.LOADING))
            {
                promise.complete(ActionStatus.failedWithMessage("Loading project is in progress in the backend. Did not reinitiated."));
            }
            else if(projectLoaderStatus.equals(ProjectLoaderStatus.ERROR))
            {
                promise.complete(ActionStatus.failedWithMessage("LoaderStatus with error message when loading project " + projectName + " .Loading project aborted."));
            }
        }

        return promise.future();
    }


    /**
     * Get status of loading a project
     *
     * GET http://localhost:{port}/:annotation_type/projects/:project_name/loadingstatus
     *
     * Example:
     * GET http://localhost:{port}/seg/projects/helloworld/loadingstatus
     */
    @GET
    @Path("/{project_name}/loadingstatus")
    @Produces(MediaType.APPLICATION_JSON)
    public LoadingStatusResponse loadProjectStatus(@PathParam("annotation_type") String annotationType,
                                                           @PathParam("project_name") String projectName)
    {
        AnnotationType type = AnnotationHandler.getTypeFromEndpoint(annotationType);
        ProjectLoader projectLoader = ProjectHandler.getProjectLoader(projectName, type);

        if (helper.checkIfProjectNull(projectLoader)) {
            return LoadingStatusResponse.builder()
                    .message(ProjectLoaderStatus.ERROR.ordinal())
                    .errorMessage("Project not exist")
                    .build();
        }

        ProjectLoaderStatus projectLoaderStatus = projectLoader.getProjectLoaderStatus();

        if (projectLoaderStatus.equals(ProjectLoaderStatus.LOADING)) {
            return LoadingStatusResponse.builder()
                    .message(projectLoaderStatus.ordinal())
                    .progress(projectLoader.getProgress())
                    .build();

        } else if (projectLoaderStatus.equals(ProjectLoaderStatus.LOADED)) {

            // Remove empty string from label list
            projectLoader.getLabelList().removeAll(Collections.singletonList(""));

            // Remove empty string from label list
            projectLoader.getLabelList().removeAll(Collections.singletonList(""));

            return LoadingStatusResponse.builder()
                    .message(projectLoaderStatus.ordinal())
                    .labelList(projectLoader.getLabelList())
                    .sanityUuidList(projectLoader.getSanityUuidList())
                    .build();

        }

        return LoadingStatusResponse.builder()
                .message(ProjectLoaderStatus.ERROR.ordinal())
                .errorMessage("Loading failed. LoaderStatus error for project " + projectName)
                .build();

    }

    /**
     * Retrieve thumbnail with metadata
     *
     * GET http://localhost:{port}/:annotation_type/projects/:project_name/uuid/:uuid/thumbnail
     *
     */
    @GET
    @Path("/{project_name}/uuid/{uuid}/thumbnail")
    @Produces(MediaType.APPLICATION_JSON)
    public Future<ThumbnailProperties> getThumbnail(@PathParam("annotation_type") String annotationType,
                                                    @PathParam("project_name") String projectName ,
                                                    @PathParam("uuid") String uuid)
    {
        AnnotationType type = AnnotationHandler.getTypeFromEndpoint(annotationType);
        String projectID = ProjectHandler.getProjectId(projectName, type.ordinal());

        return portfolioDB.getThumbnail(projectID, uuid);
    }

    /***
     *
     * Get Image Source
     *
     * GET http://localhost:{port}/:annotation_type/projects/:project_name/uuid/:uuid/imgsrc
     *
     */
    @GET
    @Path("/{project_name}/uuid/{uuid}/imgsrc")
    @Produces(MediaType.APPLICATION_JSON)
    public Future<ImageSourceResponse> getImageSource(@PathParam("annotation_type") String annotationType,
                                                      @PathParam("project_name") String projectName,
                                                      @PathParam("uuid") String uuid)
    {
        AnnotationType type = AnnotationHandler.getTypeFromEndpoint(annotationType);
        String projectID = ProjectHandler.getProjectId(projectName, type.ordinal());

        return portfolioDB.getImageSource(projectID, uuid, projectName)
                .map(result -> ImageSourceResponse.builder()
                        .message(ReplyHandler.getSUCCESSFUL())
                        .imgSrc(result)
                        .build())
                .otherwise(ImageSourceResponse.builder()
                        .message(ReplyHandler.getFAILED())
                        .errorMessage("Fail getting image source")
                        .build());
    }

    /***
     *
     * Update labelling information
     *
     * PUT http://localhost:{port}/:annotation_type/projects/:project_name/uuid/:uuid/update
     *
     */
    @PUT
    @Path("/{project_name}/uuid/{uuid}/update")
    @RequestReader(DataUpdateReader.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Future<ActionStatus> updateData(@PathParam("annotation_type") String annotationType,
                                           @PathParam("project_name") String projectName,
                                           @PathParam("uuid") String uuid,
                                           ThumbnailProperties requestBody)
    {
        AnnotationType type = AnnotationHandler.getTypeFromEndpoint(annotationType);

        String projectID = ProjectHandler.getProjectId(projectName, type.ordinal());

        ProjectLoader loader = Objects.requireNonNull(ProjectHandler.getProjectLoader(projectID));

        if(helper.checkIfProjectNull(projectID)) {
            return HTTPResponseHandler.nullProjectResponse();
        }

        return portfolioDB.updateData(requestBody, projectID)
                .map(result -> {
                    updateLastModifiedDate(loader);
                    return ActionStatus.ok();
                })
                .otherwise(ActionStatus.failedWithMessage("Failure in updating database for " + type + " project: " + projectName));
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
    @PUT
    @Path("/{project_name}/newlabels")
    @RequestReader(LabelListReader.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Future<ActionStatus> updateLabels(@PathParam("annotation_type") String annotationType,
                                             @PathParam("project_name") String projectName,
                                             LabelListBody requestBody)
    {
        AnnotationType type = AnnotationHandler.getTypeFromEndpoint(annotationType);
        String projectID = ProjectHandler.getProjectId(projectName, type.ordinal());

        if(helper.checkIfProjectNull(projectID)) {
            return HTTPResponseHandler.nullProjectResponse();
        };

        return portfolioDB.updateLabels(projectID, requestBody.getLabelList())
                .map(ActionStatus.ok())
                .otherwise(ActionStatus.failedWithMessage("Fail to update labels: " + projectName));
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
    @DELETE
    @Path("/{project_name}")
    @Produces(MediaType.APPLICATION_JSON)
    public Future<ActionStatus> deleteProject(@PathParam("annotation_type") String annotationType,
                                              @PathParam("project_name") String projectName)
    {
        AnnotationType type = AnnotationHandler.getTypeFromEndpoint(annotationType);
        String projectID = ProjectHandler.getProjectId(projectName, type.ordinal());

        if(helper.checkIfProjectNull(projectID)) {
            return HTTPResponseHandler.nullProjectResponse();
        }

        String errorMessage = "Failure in delete project name: " + projectName + " for " + type.name();

        return portfolioDB.deleteProjectFromPortfolioDb(projectID)
                .compose(result -> portfolioDB.deleteProjectFromAnnotationDb(projectID))
                .map(result -> {
                    ProjectHandler.deleteProjectFromCache(projectID);
                    return ActionStatus.ok();
                })
                .onFailure(cause -> ActionStatus.failedWithMessage(errorMessage));
    }

}
