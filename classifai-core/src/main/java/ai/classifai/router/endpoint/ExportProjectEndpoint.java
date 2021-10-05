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

import ai.classifai.action.ActionConfig;
import ai.classifai.action.ProjectExport;
import ai.classifai.database.portfolio.PortfolioDB;
import ai.classifai.dto.api.response.ExportStatusResponse;
import ai.classifai.util.http.ActionStatus;
import ai.classifai.util.http.HTTPResponseHandler;
import ai.classifai.util.message.ReplyHandler;
import ai.classifai.util.project.ProjectHandler;
import ai.classifai.util.type.AnnotationHandler;
import ai.classifai.util.type.AnnotationType;
import io.vertx.core.Future;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Classifai v2 endpoints
 *
 * @author devenyantis
 */
@Slf4j
@Builder
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ExportProjectEndpoint {

    private PortfolioDB portfolioDB;

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
        AnnotationType type = AnnotationHandler.getTypeFromEndpoint(annotationType);

        String projectId = ProjectHandler.getProjectId(projectName, type.ordinal());

        if(projectId == null) {
            return HTTPResponseHandler.nullProjectResponse();
        }

        ActionConfig.ExportType exportType = ProjectExport.getExportType(exportTypeVar);
        if(exportType.equals(ActionConfig.ExportType.INVALID_CONFIG)) {
            return HTTPResponseHandler.nullProjectResponse();
        }

        return portfolioDB.exportProject(projectId, exportType.ordinal())
                .map(ActionStatus.ok())
                .otherwise(cause -> {
                    ProjectExport.setExportStatus(ProjectExport.ProjectExportStatus.EXPORT_FAIL);
                    return ActionStatus.failedWithMessage("Export of project failed for " + projectName);
                });
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
        ProjectExport.ProjectExportStatus exportStatus = ProjectExport.getExportStatus();

        ExportStatusResponse response = ExportStatusResponse.builder()
                .message(ReplyHandler.SUCCESSFUL)
                .exportStatus(exportStatus.ordinal())
                .exportStatusMessage(exportStatus.name())
                .build();

        if(exportStatus.equals(ProjectExport.ProjectExportStatus.EXPORT_SUCCESS))
        {
            response.setProjectConfigPath(ProjectExport.getExportPath());
        }

       return response;
    }

}
