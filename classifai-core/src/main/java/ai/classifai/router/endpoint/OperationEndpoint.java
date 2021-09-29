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
package ai.classifai.router.endpoint;

import ai.classifai.database.portfolio.PortfolioDB;
import ai.classifai.database.versioning.Version;
import ai.classifai.dto.api.reader.DataUpdateReader;
import ai.classifai.dto.api.reader.LabelListReader;
import ai.classifai.dto.api.reader.body.LabelListBody;
import ai.classifai.dto.data.ThumbnailProperties;
import ai.classifai.loader.ProjectLoader;
import ai.classifai.util.datetime.DateTime;
import ai.classifai.util.http.ActionStatus;
import ai.classifai.util.http.HTTPResponseHandler;
import ai.classifai.util.project.ProjectHandler;
import ai.classifai.util.type.AnnotationHandler;
import ai.classifai.util.type.AnnotationType;
import com.zandero.rest.annotation.RequestReader;
import io.vertx.core.Future;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Objects;

/**
 * Classifai v1 endpoints
 *
 * @author devenyantis
 */
@Slf4j
@Path("/{annotation_type}/projects")
public class OperationEndpoint {
    @Setter
    private PortfolioDB portfolioDB;

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

        if(projectID == null) {
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

        if(projectID == null) {
            return HTTPResponseHandler.nullProjectResponse();
        }

        return portfolioDB.updateLabels(projectID, requestBody.getLabelList())
                .map(ActionStatus.ok())
                .otherwise(ActionStatus.failedWithMessage("Fail to update labels: " + projectName));
    }




}
