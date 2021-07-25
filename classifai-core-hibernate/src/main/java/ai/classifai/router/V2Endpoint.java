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

import ai.classifai.selector.project.LabelFileSelector;
import ai.classifai.selector.project.ProjectFolderSelector;
import ai.classifai.selector.status.FileSystemStatus;
import ai.classifai.selector.status.SelectionWindowStatus;
import ai.classifai.util.ParamConfig;
import ai.classifai.util.http.HTTPResponseHandler;
import ai.classifai.util.message.ReplyHandler;
import ai.classifai.util.type.AnnotationType;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;

/**
 * Classifai v2 endpoints
 *
 * @author devenyantis
 */
@Slf4j
public class V2Endpoint extends EndpointBase {







//
//    /***
//     * export a project to configuration file
//     *
//     * PUT http://localhost:{port}/v2/:annotation_type/projects/:project_name/export/:export_type
//     */
//    public void exportProject(RoutingContext context)
//    {
//        AnnotationType type = AnnotationHandler.getTypeFromEndpoint(context.request().getParam(ParamConfig.getAnnotationTypeParam()));
//
//        String name = context.request().getParam(ParamConfig.getProjectNameParam());
//        String id = ProjectHandler.getId(name, type.ordinal());
//
//        if(helper.checkIfProjectNull(context, id, name)) return;
//
//        ActionConfig.ExportType exportType = ProjectExport.getExportType(
//                context.request().getParam(ActionConfig.getExportTypeParam()));
//        if(exportType.equals(ActionConfig.ExportType.INVALID_CONFIG)) return;
//
//        JsonObject request = new JsonObject()
//                .put(ParamConfig.getProjectIdParam(), id)
//                .put(ParamConfig.getAnnotationTypeParam(), type.ordinal())
//                .put(ActionConfig.getExportTypeParam(), exportType.ordinal());
//
//        DeliveryOptions options = new DeliveryOptions().addHeader(ParamConfig.getActionKeyword(), PortfolioDbQuery.getExportProject());
//
//        // Initiate export status
//        ProjectExport.setExportStatus(ProjectExport.ProjectExportStatus.EXPORT_STARTING);
//        ProjectExport.setExportPath("");
//
//        vertx.eventBus().request(PortfolioDbQuery.getQueue(), request, options, reply -> {
//
//            if (reply.succeeded()) {
//
//                JsonObject response = (JsonObject) reply.result().body();
//
//                HTTPResponseHandler.configureOK(context, response);
//            }
//            else
//            {
//                HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError("Export of project failed for " + name));
//                ProjectExport.setExportStatus(ProjectExport.ProjectExportStatus.EXPORT_FAIL);
//            }
//        });
//
//    }
//
//    /**
//     * Get export project status
//     * GET http://localhost:{port}/v2/:annotation_type/projects/exportstatus
//     *
//     * Example:
//     * GET http://localhost:{port}/v2/bndbox/projects/exportstatus
//     *
//     */
//    public void getExportStatus(RoutingContext context)
//    {
//        helper.checkIfDockerEnv(context);
//
//        ProjectExport.ProjectExportStatus exportStatus = ProjectExport.getExportStatus();
//        JsonObject response = ReplyHandler.getOkReply();
//        response.put(ActionConfig.getExportStatusParam(), exportStatus.ordinal());
//        response.put(ActionConfig.getExportStatusMessageParam(), exportStatus.name());
//
//        if(exportStatus.equals(ProjectExport.ProjectExportStatus.EXPORT_SUCCESS))
//        {
//            response.put(ActionConfig.getProjectConfigPathParam(), ProjectExport.getExportPath());
//        }
//
//        HTTPResponseHandler.configureOK(context, response);
//    }
//
//    public void importProject(RoutingContext context)
//    {
//        if(projectImporter.isWindowOpen())
//        {
//            JsonObject jsonResonse = ReplyHandler.reportUserDefinedError("Import config file selector window has already opened. Close that to proceed.");
//
//            HTTPResponseHandler.configureOK(context, jsonResonse);
//        }
//        else
//        {
//            HTTPResponseHandler.configureOK(context);
//        }
//
//        projectImporter.run();
//    }
//
//    /**
//     * Get import project status
//     * GET http://localhost:{port}/v2/:annotation_type/projects/importstatus
//     *
//     * Example:
//     * GET http://localhost:{port}/v2/bndbox/projects/importstatus
//     *
//     */
//    public void getImportStatus(RoutingContext context)
//    {
//        helper.checkIfDockerEnv(context);
//
//        FileSystemStatus fileSysStatus = ProjectImportSelector.getImportFileSystemStatus();
//        JsonObject response = compileFileSysStatusResponse(fileSysStatus);
//
//        if(fileSysStatus.equals(FileSystemStatus.DATABASE_UPDATED))
//        {
//            response.put(ParamConfig.getProjectNameParam(), ProjectImportSelector.getProjectNameParam());
//        }
//
//        HTTPResponseHandler.configureOK(context, response);
//    }


}
