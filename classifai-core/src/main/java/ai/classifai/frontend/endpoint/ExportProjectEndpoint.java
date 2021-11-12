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

import ai.classifai.core.ExportProjectService;
import ai.classifai.core.entities.response.ExportStatusResponse;
import ai.classifai.core.enums.ExportType;
import ai.classifai.core.enums.ProjectExportStatus;
import ai.classifai.core.util.http.ActionStatus;
import ai.classifai.core.util.http.HTTPResponseHandler;
import ai.classifai.core.util.message.ReplyHandler;
import ai.classifai.core.util.type.AnnotationType;
import io.vertx.core.Future;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Classifai v2 endpoints
 *
 * @author devenyantis
 */
@Slf4j
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ExportProjectEndpoint {

    private final ExportProjectService exportProjectServices;

    public ExportProjectEndpoint(ExportProjectService exportProjectServices) {
        this.exportProjectServices = exportProjectServices;
    }

    /***
     * export a project to configuration file
     *
     * PUT http://localhost:{port}/v2/:annotation_type/projects/:project_name/export/:export_type
     */
    @PUT
    @Path("/v2/{annotation_type}/projects/{project_name}/export/{export_type}")
    public Future<ActionStatus> exportProject(@PathParam("annotation_type") String annotationType,
                                              @PathParam("project_name") String projectName,
                                              @PathParam("export_type") String exportTypeVar)
    {
        AnnotationType type = AnnotationType.getTypeFromEndpoint(annotationType);

        String projectId = exportProjectServices.getProjectId(projectName, type.ordinal());

        if(projectId == null) {
            return HTTPResponseHandler.nullProjectResponse();
        }

        ExportType exportType = exportProjectServices.getExportType(exportTypeVar);
        if(exportType.equals(ExportType.INVALID_CONFIG)) {
            return HTTPResponseHandler.nullProjectResponse();
        }

        exportProjectServices.setExportStatus(ProjectExportStatus.EXPORT_STARTING);

        return exportProjectServices.exportProject(projectId, exportType.ordinal(), projectName);

    }

    /**
     * Get export project status
     * GET http://localhost:{port}/v2/:annotation_type/projects/exportstatus
     *
     * Example:
     * GET http://localhost:{port}/v2/bndbox/projects/exportstatus
     *
     */
    @GET
    @Path("/v2/{annotation_type}/projects/exportstatus")
    public ExportStatusResponse getExportStatus()
    {
        ProjectExportStatus exportStatus = exportProjectServices.getExportStatus();

        ExportStatusResponse response = ExportStatusResponse.builder()
                .message(ReplyHandler.SUCCESSFUL)
                .exportStatus(exportStatus.ordinal())
                .exportStatusMessage(exportStatus.name())
                .build();

        if(exportStatus.equals(ProjectExportStatus.EXPORT_SUCCESS))
        {
            response.setProjectConfigPath(exportProjectServices.getExportPath());
            exportProjectServices.setExportStatus(ProjectExportStatus.EXPORT_NOT_INITIATED);
        }

       return response;
    }

}
