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
package ai.classifai.frontend.endpoint;

import ai.classifai.core.ProjectOperationService;
import ai.classifai.core.entities.dto.LabelListDTO;
import ai.classifai.core.entities.properties.ThumbnailProperties;
import ai.classifai.core.loader.ProjectLoader;
import ai.classifai.core.util.http.ActionStatus;
import ai.classifai.core.util.http.HTTPResponseHandler;
import ai.classifai.core.util.type.AnnotationType;
import io.vertx.core.Future;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Classifai v1 endpoints
 *
 * @author devenyantis
 */
@Slf4j
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class OperationEndpoint {
    private final ProjectOperationService projectOperationService;

    public OperationEndpoint(ProjectOperationService projectOperationService) {
        this.projectOperationService = projectOperationService;
    }

    /***
     *
     * Update labelling information
     *
     * PUT http://localhost:{port}/:annotation_type/projects/:project_name/uuid/:uuid/update
     *
     */
    @PUT
    @Path("/{annotation_type}/projects/{project_name}/uuid/{uuid}/update")
    public Future<ActionStatus> updateData(@PathParam("annotation_type") String annotationType,
                                           @PathParam("project_name") String projectName,
                                           @PathParam("uuid") String uuid,
                                           ThumbnailProperties requestBody)
    {
        AnnotationType type = AnnotationType.getTypeFromEndpoint(annotationType);

        ProjectLoader loader = projectOperationService.getProjectLoader(projectName, type);

        if(loader == null) {
            return HTTPResponseHandler.nullProjectResponse();
        }

        return projectOperationService.updateData(requestBody, loader);
    }


    /***
     *
     * Update labels
     *
     * PUT http://localhost:{port}/:annotation_type/projects/:project_name/newlabels
     *
     */
    @PUT
    @Path("/{annotation_type}/projects/{project_name}/newlabels")
    public Future<ActionStatus> updateLabels(@PathParam("annotation_type") String annotationType,
                                             @PathParam("project_name") String projectName,
                                             LabelListDTO requestBody)
    {
        AnnotationType type = AnnotationType.getTypeFromEndpoint(annotationType);
        String projectID = projectOperationService.getProjectId(projectName, type.ordinal());

        if(projectID == null) {
            return HTTPResponseHandler.nullProjectResponse();
        }

        return projectOperationService.updateLabels(requestBody, projectID, projectName);
    }




}
