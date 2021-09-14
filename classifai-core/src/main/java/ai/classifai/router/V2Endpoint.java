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

import ai.classifai.action.ActionConfig;
import ai.classifai.action.LabelListImport;
import ai.classifai.action.ProjectExport;
import ai.classifai.database.portfolio.PortfolioDB;
import ai.classifai.loader.ProjectLoader;
import ai.classifai.loader.ProjectLoaderStatus;
import ai.classifai.selector.project.LabelFileSelector;
import ai.classifai.selector.project.ProjectFolderSelector;
import ai.classifai.selector.project.ProjectImportSelector;
import ai.classifai.selector.status.FileSystemStatus;
import ai.classifai.selector.status.NewProjectStatus;
import ai.classifai.selector.status.SelectionWindowStatus;
import ai.classifai.util.ParamConfig;
import ai.classifai.util.collection.ConversionHandler;
import ai.classifai.util.collection.UuidGenerator;
import ai.classifai.util.http.HTTPResponseHandler;
import ai.classifai.util.message.ReplyHandler;
import ai.classifai.util.project.ProjectHandler;
import ai.classifai.util.project.ProjectInfra;
import ai.classifai.util.type.AnnotationHandler;
import ai.classifai.util.type.AnnotationType;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

/**
 * Classifai v2 endpoints
 *
 * @author devenyantis
 */
@Slf4j
public class V2Endpoint extends EndpointBase {

    @Setter private ProjectFolderSelector projectFolderSelector = null;
    @Setter private ProjectImportSelector projectImporter = null;

    @Setter private LabelFileSelector labelFileSelector = null;

    @Setter private PortfolioDB portfolioDB;

    /***
     * change is_load state of a project to false
     *
     * PUT http://localhost:{port}/:annotation_type/projects/:project_name
     */
    public void closeProjectState(RoutingContext context)
    {
        AnnotationType type = AnnotationHandler.getTypeFromEndpoint(context.request().getParam(ParamConfig.getAnnotationTypeParam()));

        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());
        String projectID = ProjectHandler.getProjectId(projectName, type.ordinal());

        if(helper.checkIfProjectNull(context, projectID, projectName)) return;

        context.request().bodyHandler(h ->
        {
            try
            {
                JsonObject jsonObject = h.toJsonObject();

                if(jsonObject.getString(ParamConfig.getStatusParam()).equals("closed"))
                {
                    ProjectHandler.getProjectLoader(projectID).setIsLoadedFrontEndToggle(Boolean.FALSE);
                }
                else
                {
                    throw new IllegalArgumentException("Request payload failed to satisfied the status of {\"status\": \"closed\"} for " + projectName + ". ");
                }

                HTTPResponseHandler.configureOK(context);

            }
            catch (Exception e)
            {
                HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError(e.getMessage()));

            }
        });
    }

    /***
     * Star a project
     *
     * PUT http://localhost:{port}/:annotation_type/projects/:projectname/star
     */
    public void starProject(RoutingContext context)
    {
        AnnotationType type = AnnotationHandler.getTypeFromEndpoint(context.request().getParam(ParamConfig.getAnnotationTypeParam()));

        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());
        String projectID = ProjectHandler.getProjectId(projectName, type.ordinal());

        if(helper.checkIfProjectNull(context, projectID, projectName)) return;

        context.request().bodyHandler(handler -> {
            JsonObject request = handler.toJsonObject();
            Boolean isStarred = Boolean.parseBoolean(request.getString(ParamConfig.getStatusParam()));
            Future<JsonObject> future = portfolioDB.starProject(projectID, isStarred);
            ReplyHandler.sendResultRunSuccessSideEffect(context, future,
                    ()-> {
                        ProjectLoader loader = Objects.requireNonNull(ProjectHandler.getProjectLoader(projectID));
                        loader.setIsProjectStarred(isStarred);
                    },
                    "Star project fail");
        });
    }

    /**
     * Create new project
     * PUT http://localhost:{port}/v2/projects
     *
     * Request Body
     * create raw project
     * {
     *   "status": "raw",
     *   "project_name": "test-project",
     *   "annotation_type": "boundingbox",
     *   "project_path": "/Users/codenamwei/Desktop/Education/books",
     *   "label_file_path": "/Users/codenamewei/Downloads/test_label.txt",
     * }
     *
     * create config project
     * {
     *   "status": "config"
     * }
     */
    public void createProject(RoutingContext context)
    {
        context.request().bodyHandler(h ->
        {
            try
            {
                JsonObject requestBody = h.toJsonObject();

                String projectStatus = requestBody.getString(ParamConfig.getStatusParam()).toUpperCase();

                if(projectStatus.equals(NewProjectStatus.CONFIG.name()))
                {
                    createConfigProject(context);
                }
                else if(projectStatus.equals(NewProjectStatus.RAW.name()))
                {
                    createRawProject(requestBody, context);
                }
                else
                {
                    String errorMessage = "Project status: " + projectStatus + " not recognizable. Expect " + NewProjectStatus.getParamList();

                    HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError(errorMessage));
                }
            }
            catch(Exception e)
            {
                String errorMessage = "Parameter of status with " + NewProjectStatus.getParamList() + " is compulsory in request body";
                HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError(errorMessage));

            }
        });
    }

    protected void createConfigProject(RoutingContext context)
    {
        if(projectImporter.isWindowOpen())
        {
            JsonObject jsonResponse = ReplyHandler.reportUserDefinedError("Import config file selector window has already opened. Close that to proceed.");

            HTTPResponseHandler.configureOK(context, jsonResponse);
        }
        else
        {
            HTTPResponseHandler.configureOK(context);
        }

        projectImporter.run();
    }

    protected void createRawProject(JsonObject requestBody, RoutingContext context) throws IOException {
        String projectName = requestBody.getString(ParamConfig.getProjectNameParam());

        String annotationName = requestBody.getString(ParamConfig.getAnnotationTypeParam());
        Integer annotationInt = AnnotationHandler.getType(annotationName).ordinal();

        if (ProjectHandler.isProjectNameUnique(projectName, annotationInt))
        {
            String projectPath = requestBody.getString(ParamConfig.getProjectPathParam());

            String labelPath = requestBody.getString(ParamConfig.getLabelPathParam());
            List<String> labelList = new LabelListImport(new File(labelPath)).getValidLabelList();

            ProjectLoader loader = ProjectLoader.builder()
                    .projectId(UuidGenerator.generateUuid())
                    .projectName(projectName)
                    .annotationType(annotationInt)
                    .projectPath(new File(projectPath))
                    .labelList(labelList)
                    .projectLoaderStatus(ProjectLoaderStatus.LOADED)
                    .projectInfra(ProjectInfra.ON_PREMISE)
                    .fileSystemStatus(FileSystemStatus.ITERATING_FOLDER)
                    .build();

            ProjectHandler.loadProjectLoader(loader);

            loader.initFolderIteration();

            HTTPResponseHandler.configureOK(context);
        }
        else
        {
            HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError("Project name exist: " + projectName));
        }
    }

    /**
     * Create new project status
     * GET http://localhost:{port}/v2/:annotation_type/projects/:project_name
     *
     * Example:
     * GET http://localhost:{port}/v2/bndbox/projects/helloworld
     */
    public void createProjectStatus(RoutingContext context)
    {
        String annotationName = context.request().getParam(ParamConfig.getAnnotationTypeParam());
        AnnotationType type = AnnotationHandler.getTypeFromEndpoint(annotationName);

        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());

        ProjectLoader loader = ProjectHandler.getProjectLoader(projectName, type);

        FileSystemStatus status = loader.getFileSystemStatus();

        JsonObject response = compileFileSysStatusResponse(status);

        if (status.equals(FileSystemStatus.DATABASE_UPDATED))
        {
            response.put(ParamConfig.getUnsupportedImageListParam(), loader.getUnsupportedImageList());
        }

        HTTPResponseHandler.configureOK(context, response);
    }

    /**
     * Rename project
     * PUT http://localhost:{port}/v2/:annotation_type/rename/:project_name/:new_project_name
     *
     * Example:
     * PUT http://localhost:{port}/v2/bndbox/rename/helloworld/helloworldnewname
     *
     */
    public void renameProject(RoutingContext context)
    {
        AnnotationType type = AnnotationHandler.getTypeFromEndpoint(context.request().getParam(ParamConfig.getAnnotationTypeParam()));

        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());
        String newProjectName = context.request().getParam(ParamConfig.getNewProjectNameParam());

        ProjectLoader loader = ProjectHandler.getProjectLoader(projectName, type);

        if(helper.checkIfProjectNull(context, loader, projectName)) return;

        if(ProjectHandler.checkValidProjectRename(newProjectName, type.ordinal()))
        {
            Future<JsonObject> future = portfolioDB.renameProject(loader.getProjectId(), newProjectName);
            ReplyHandler.sendEmptyResult(context, future, () -> {
                loader.setProjectName(newProjectName);
                ProjectHandler.updateProjectNameInCache(loader.getProjectId(), loader, projectName);
                log.debug("Rename to " + newProjectName + " success.");
            }, "Failed to rename project " + projectName);
        }
        else
        {
            HTTPResponseHandler.configureOK(context);
        }
    }

    /**
     * Reload v2 project
     * PUT http://localhost:{port}/v2/:annotation_type/projects/:project_name/reload
     *
     * Example:
     * PUT http://localhost:{port}/v2/bndbox/projects/helloworld/reload
     *
     */
    public void reloadProject(RoutingContext context)
    {
        AnnotationType type = AnnotationHandler.getTypeFromEndpoint(context.request().getParam(ParamConfig.getAnnotationTypeParam()));

        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());

        log.info("Reloading project: " + projectName + " of annotation type: " + type.name());

        ProjectLoader loader = ProjectHandler.getProjectLoader(projectName, type);

        if(helper.checkIfProjectNull(context, loader, projectName)) return;

        loader.setFileSystemStatus(FileSystemStatus.ITERATING_FOLDER);

        Future<JsonObject> future = portfolioDB.reloadProject(loader.getProjectId());
        ReplyHandler.sendEmptyResult(context, future, "Failed to reload project " + projectName);
    }

    /**
     * Get load status of project
     * GET http://localhost:{port}/v2/:annotation_type/projects/:project_name/reloadstatus
     *
     * Example:
     * GET http://localhost:{port}/v2/bndbox/projects/helloworld/reloadstatus
     *
     */
    public void reloadProjectStatus(RoutingContext context)
    {
        AnnotationType type = AnnotationHandler.getTypeFromEndpoint(context.request().getParam(ParamConfig.getAnnotationTypeParam()));

        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());

        ProjectLoader loader = ProjectHandler.getProjectLoader(projectName, type);

        if(helper.checkIfProjectNull(context, loader, projectName)) return;

        FileSystemStatus fileSysStatus = loader.getFileSystemStatus();

        JsonObject res = compileFileSysStatusResponse(fileSysStatus);

        if(fileSysStatus.equals(FileSystemStatus.DATABASE_UPDATING))
        {
            res.put(ParamConfig.getProgressMetadata(), loader.getProgressUpdate());
        }
        else if(fileSysStatus.equals(FileSystemStatus.DATABASE_UPDATED))
        {
            res.put(ParamConfig.getUuidAdditionListParam(), loader.getReloadAdditionList());
            res.put(ParamConfig.getUuidDeletionListParam(), loader.getReloadDeletionList());
            res.put(ParamConfig.getUnsupportedImageListParam(), loader.getUnsupportedImageList());
        }

        HTTPResponseHandler.configureOK(context, res);
    }

    /***
     * export a project to configuration file
     *
     * PUT http://localhost:{port}/v2/:annotation_type/projects/:project_name/export/:export_type
     */
    public void exportProject(RoutingContext context)
    {
        AnnotationType type = AnnotationHandler.getTypeFromEndpoint(context.request().getParam(ParamConfig.getAnnotationTypeParam()));

        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());
        String projectId = ProjectHandler.getProjectId(projectName, type.ordinal());

        if(helper.checkIfProjectNull(context, projectId, projectName)) return;

        ActionConfig.ExportType exportType = ProjectExport.getExportType(
                context.request().getParam(ActionConfig.getExportTypeParam()));
        if(exportType.equals(ActionConfig.ExportType.INVALID_CONFIG)) return;

        Future<JsonObject> future = portfolioDB.exportProject(projectId, exportType.ordinal());
        ReplyHandler.sendResultRunFailSideEffect(context, future,
                () -> ProjectExport.setExportStatus(ProjectExport.ProjectExportStatus.EXPORT_FAIL),
                "Export of project failed for " + projectName);
    }

    /**
     * Get export project status
     * GET http://localhost:{port}/v2/:annotation_type/projects/exportstatus
     *
     * Example:
     * GET http://localhost:{port}/v2/bndbox/projects/exportstatus
     *
     */
    public void getExportStatus(RoutingContext context)
    {
        helper.checkIfDockerEnv(context);

        ProjectExport.ProjectExportStatus exportStatus = ProjectExport.getExportStatus();
        JsonObject response = ReplyHandler.getOkReply();
        response.put(ActionConfig.getExportStatusParam(), exportStatus.ordinal());
        response.put(ActionConfig.getExportStatusMessageParam(), exportStatus.name());

        if(exportStatus.equals(ProjectExport.ProjectExportStatus.EXPORT_SUCCESS))
        {
            response.put(ActionConfig.getProjectConfigPathParam(), ProjectExport.getExportPath());
        }

        HTTPResponseHandler.configureOK(context, response);
    }

    /**
     * Get import project status
     * GET http://localhost:{port}/v2/:annotation_type/projects/importstatus
     *
     * Example:
     * GET http://localhost:{port}/v2/bndbox/projects/importstatus
     *
     */
    public void getImportStatus(RoutingContext context)
    {
        helper.checkIfDockerEnv(context);

        FileSystemStatus fileSysStatus = ProjectImportSelector.getImportFileSystemStatus();
        JsonObject response = compileFileSysStatusResponse(fileSysStatus);

        if(fileSysStatus.equals(FileSystemStatus.DATABASE_UPDATED))
        {
            response.put(ParamConfig.getProjectNameParam(), ProjectImportSelector.getProjectName());
        }

        HTTPResponseHandler.configureOK(context, response);
    }

    /**
     * Initiate load label list
     * PUT http://localhost:{port}/v2/labelfiles
     *
     * Example:
     * PUT http://localhost:{port}/v2/labelfiles
     */
    public void selectLabelFile(RoutingContext context)
    {
        helper.checkIfDockerEnv(context);

        if(!labelFileSelector.isWindowOpen())
        {
            labelFileSelector.run();

        }

        HTTPResponseHandler.configureOK(context);
    }

    /**
     * Get load label file status
     * GET http://localhost:{port}/v2/labelfiles
     *
     * Example:
     * GET http://localhost:{port}/v2/labelfiles
     */
    public void selectLabelFileStatus(RoutingContext context)
    {
        helper.checkIfDockerEnv(context);

        SelectionWindowStatus status = labelFileSelector.getWindowStatus();

        JsonObject jsonResponse = compileSelectionWindowResponse(status);

        if(status.equals(SelectionWindowStatus.WINDOW_CLOSE))
        {
            jsonResponse.put(ParamConfig.getLabelPathParam(), labelFileSelector.getLabelFilePath());
        }

        HTTPResponseHandler.configureOK(context, jsonResponse);
    }


    /**
     * Open folder selector to choose project folder
     * PUT http://localhost:{port}/v2/folders
     *
     * Example:
     * PUT http://localhost:{port}/v2/folders
     */
    public void selectProjectFolder(RoutingContext context)
    {
        helper.checkIfDockerEnv(context);

        if(!projectFolderSelector.isWindowOpen())
        {
            projectFolderSelector.run();

        }
        HTTPResponseHandler.configureOK(context);
    }

    /**
     * Close/terminate classifai
     * PUT http://localhost:{port}/v2/close
     *
     * Example:
     * PUT http://localhost:{port}/v2/close
     */
    public void closeClassifai(RoutingContext context)
    {
        HTTPResponseHandler.configureOK(context);

        //terminate after 1 seconds
        new java.util.Timer().schedule(
                new java.util.TimerTask() {
                    @Override
                    public void run() {
                        System.exit(0);
                    }
                },
                1000
        );
    }

    /**
     * Get status of choosing a project folder
     * GET http://localhost:{port}/v2/folders
     *
     * Example:
     * GET http://localhost:{port}/v2/folders
     */
    public void selectProjectFolderStatus(RoutingContext context)
    {
        helper.checkIfDockerEnv(context);

        SelectionWindowStatus status = projectFolderSelector.getWindowStatus();

        JsonObject jsonResponse = compileSelectionWindowResponse(status);

        if(status.equals(SelectionWindowStatus.WINDOW_CLOSE))
        {
            jsonResponse.put(ParamConfig.getProjectPathParam(), projectFolderSelector.getProjectFolderPath());
        }

        HTTPResponseHandler.configureOK(context, jsonResponse);
    }

    /**
     * Load data based on uuid
     * DELETE http://localhost:{port}/v2/:annotation_type/projects/:project_name/uuids
     *
     * json payload = {
     *      "uuid_list": ["d99fed36-4eb5-4572-b2c7-ca8d4136e692", "d99fed36-4eb5-4572-b2c7-ca8d4136d2d3f"],
     *      "img_path_list": [
     *              "C:\Users\Deven.Yantis\Desktop\classifai-car-images\12.jpg",
     *              "C:\Users\Deven.Yantis\Desktop\classifai-car-images\1.jpg"
     *       ]
     *  }
     *
     * Example:
     * GET http://localhost:{port}/v2/bndbox/projects/helloworld/uuids
     */
    public void deleteProjectData(RoutingContext context)
    {
        AnnotationType type = AnnotationHandler.getTypeFromEndpoint(context.request().getParam(ParamConfig.getAnnotationTypeParam()));

        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());

        String projectID = ProjectHandler.getProjectId(projectName, type.ordinal());

        if(helper.checkIfProjectNull(context, projectID, projectName)) return;

        context.request().bodyHandler(handler -> {
            JsonObject request = Objects.requireNonNull(ConversionHandler.json2JSONObject(handler.toJson()));

            Future<JsonObject> future = portfolioDB.deleteProjectData(
                    projectID,
                    request.getJsonArray(ParamConfig.getUuidListParam()),
                    request.getJsonArray(ParamConfig.getImgPathListParam())
            );
            ReplyHandler.sendResult(context, future, "Delete project data fail.");
        });
    }

    /**
     * Rename data filename
     * PUT http://localhost:{port}/v2/:annotation_type/projects/:project_name/imgsrc/rename
     *
     * Example:
     * PUT http://localhost:{port}/v2/bndbox/projects/helloworld/imgsrc/rename
     *
     * json payload = {
     *      "uuid" : "f592a6e2-53f8-4730-930c-8357d191de48"
     *      "new_fname" : "new_7.jpg"
     * }
     *
     */
    public void renameData(RoutingContext context)
    {
        AnnotationType type = AnnotationHandler.getTypeFromEndpoint(context.request().getParam(ParamConfig.getAnnotationTypeParam()));

        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());
        String projectId = ProjectHandler.getProjectId(projectName, type.ordinal());


        context.request().bodyHandler(handler -> {
            JsonObject request = Objects.requireNonNull(ConversionHandler.json2JSONObject(handler.toJson()));

            Future<JsonObject> future = portfolioDB.renameData(
                    projectId,
                    request.getString(ParamConfig.getUuidParam()),
                    request.getString(ParamConfig.getNewFileNameParam())
            );

            ReplyHandler.sendResult(context, future, "Rename data fail");
        });
    }
}
