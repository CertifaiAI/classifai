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
import ai.classifai.database.annotation.AnnotationQuery;
import ai.classifai.database.portfolio.PortfolioDbQuery;
import ai.classifai.loader.ProjectLoader;
import ai.classifai.loader.ProjectLoaderStatus;
import ai.classifai.selector.project.ImageFileSelector;
import ai.classifai.selector.project.LabelFileSelector;
import ai.classifai.selector.project.ProjectFolderSelector;
import ai.classifai.selector.project.ProjectImportSelector;
import ai.classifai.selector.status.FileSystemStatus;
import ai.classifai.selector.status.NewProjectStatus;
import ai.classifai.selector.status.SelectionWindowStatus;
import ai.classifai.util.ParamConfig;
import ai.classifai.util.collection.ConversionHandler;
import ai.classifai.util.collection.UuidGenerator;
import ai.classifai.util.data.FileHandler;
import ai.classifai.util.data.ImageHandler;
import ai.classifai.util.http.HTTPResponseHandler;
import ai.classifai.util.message.ReplyHandler;
import ai.classifai.util.project.ProjectHandler;
import ai.classifai.util.project.ProjectInfra;
import ai.classifai.util.type.AnnotationHandler;
import ai.classifai.util.type.AnnotationType;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

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
    @Setter private ImageFileSelector imageFileSelector = null;

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

        context.request().bodyHandler(h ->
        {
            JsonObject jsonObject = h.toJsonObject();

            jsonObject.put(ParamConfig.getProjectIdParam(), projectID);

            DeliveryOptions options = new DeliveryOptions().addHeader(ParamConfig.getActionKeyword(), PortfolioDbQuery.getStarProject());

            vertx.eventBus().request(PortfolioDbQuery.getQueue(), jsonObject, options, reply ->
            {
                if(reply.succeeded())
                {
                    //set status of starring to project loader.
                    Boolean isStar = Boolean.parseBoolean(jsonObject.getString(ParamConfig.getStatusParam()));
                    ProjectLoader loader = ProjectHandler.getProjectLoader(projectID);
                    loader.setIsProjectStarred(isStar);

                    JsonObject response = (JsonObject) reply.result().body();

                    HTTPResponseHandler.configureOK(context, response);
                }
            });
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
            JsonObject jsonObject = new JsonObject();
            jsonObject.put(ParamConfig.getProjectIdParam(), loader.getProjectId());
            jsonObject.put(ParamConfig.getNewProjectNameParam(), newProjectName);

            DeliveryOptions renameOps = new DeliveryOptions().addHeader(ParamConfig.getActionKeyword(), PortfolioDbQuery.getRenameProject());

            loader.setProjectName(newProjectName);

            vertx.eventBus().request(PortfolioDbQuery.getQueue(), jsonObject, renameOps, fetch ->
            {
                JsonObject response = (JsonObject) fetch.result().body();

                if (ReplyHandler.isReplyOk(response))
                {
                    // Update loader in cache after success db update
                    ProjectHandler.updateProjectNameInCache(loader.getProjectId(), loader, projectName);
                    log.debug("Rename to " + newProjectName + " success.");
                    HTTPResponseHandler.configureOK(context);
                }
                else
                {
                    HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError("Failed to rename project " + projectName));
                }
            });
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

        JsonObject jsonObject = new JsonObject().put(ParamConfig.getProjectIdParam(), loader.getProjectId());

        DeliveryOptions reloadOps = new DeliveryOptions().addHeader(ParamConfig.getActionKeyword(), PortfolioDbQuery.getReloadProject());

        vertx.eventBus().request(PortfolioDbQuery.getQueue(), jsonObject, reloadOps, fetch ->
        {
            JsonObject response = (JsonObject) fetch.result().body();

            if (ReplyHandler.isReplyOk(response))
            {
                HTTPResponseHandler.configureOK(context);

            } else
            {
                HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError("Failed to reload project " + projectName));
            }
        });
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

        JsonObject request = new JsonObject()
                .put(ParamConfig.getProjectIdParam(), projectId)
                .put(ParamConfig.getAnnotationTypeParam(), type.ordinal())
                .put(ActionConfig.getExportTypeParam(), exportType.ordinal());

        DeliveryOptions options = new DeliveryOptions().addHeader(ParamConfig.getActionKeyword(), PortfolioDbQuery.getExportProject());

        // Initiate export status
        ProjectExport.setExportStatus(ProjectExport.ProjectExportStatus.EXPORT_STARTING);
        ProjectExport.setExportPath("");

        vertx.eventBus().request(PortfolioDbQuery.getQueue(), request, options, reply -> {

            if (reply.succeeded()) {

                JsonObject response = (JsonObject) reply.result().body();

                HTTPResponseHandler.configureOK(context, response);
            }
            else
            {
                HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError("Export of project failed for " + projectName));
                ProjectExport.setExportStatus(ProjectExport.ProjectExportStatus.EXPORT_FAIL);
            }
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

        String query = AnnotationQuery.getDeleteProjectData();

        if(helper.checkIfProjectNull(context, projectID, projectName)) return;

        context.request().bodyHandler(h ->
        {
            JsonObject request = Objects.requireNonNull(ConversionHandler.json2JSONObject(h.toJson()));

            JsonArray uuidListArray = request.getJsonArray(ParamConfig.getUuidListParam());
            JsonArray uuidImgPathList = request.getJsonArray(ParamConfig.getImgPathListParam());

            request.put(ParamConfig.getProjectIdParam(), projectID);
            request.put(ParamConfig.getUuidListParam(), uuidListArray);
            request.put(ParamConfig.getImgPathListParam(), uuidImgPathList);

            DeliveryOptions options = new DeliveryOptions().addHeader(ParamConfig.getActionKeyword(), query);

            vertx.eventBus().request(helper.getDbQuery(type), request, options, reply ->
            {
                if (reply.succeeded())
                {
                    JsonObject response = (JsonObject) reply.result().body();

                    HTTPResponseHandler.configureOK(context, response);
                }
            });
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

        context.request().bodyHandler(h -> {
            JsonObject request = Objects.requireNonNull(ConversionHandler.json2JSONObject(h.toJson()));

            request.put(ParamConfig.getProjectIdParam(), projectId);
            request.put(ParamConfig.getUuidParam(), request.getString(ParamConfig.getUuidParam()));
            request.put(ParamConfig.getNewFileNameParam(), request.getString(ParamConfig.getNewFileNameParam()));

            DeliveryOptions options = new DeliveryOptions().addHeader(ParamConfig.getActionKeyword(), AnnotationQuery.getRenameProjectData());

            vertx.eventBus().request(helper.getDbQuery(type), request, options, reply -> {
                if(reply.succeeded())
                {
                    JsonObject response = (JsonObject) reply.result().body();
                    HTTPResponseHandler.configureOK(context, response);
                }
            });
        });

    }


    /**
     * Retrieve number of labeled Image, unlabeled Image and total number of labels per class in a project
     *
     * GET http://localhost:{port}/v2/:annotation_type/projects/:project_name/statistic
     *
     * Example:
     * GET http://localhost:{port}/v2/bndbox/projects/demo/statistic
     *
     */
    public void getProjectStatistic (RoutingContext context){

        AnnotationType type = AnnotationHandler.getTypeFromEndpoint(context.request().getParam(ParamConfig.getAnnotationTypeParam()));

        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());

        log.debug("Get project statistic : " + projectName + " of annotation type: " + type.name());

        ProjectLoader loader = ProjectHandler.getProjectLoader(projectName, type);

        if(helper.checkIfProjectNull(context, loader, projectName)) return;

        if(loader == null)
        {
            HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError("Failure in retrieving statistic of project: " + projectName));
        }

        JsonObject jsonObject = new JsonObject().put(ParamConfig.getProjectIdParam(), Objects.requireNonNull(loader).getProjectId());

        //load label list
        DeliveryOptions statisticDataOptions = new DeliveryOptions().addHeader(ParamConfig.getActionKeyword(), PortfolioDbQuery.getRetrieveProjectStatistic());

        vertx.eventBus().request(PortfolioDbQuery.getQueue(), jsonObject, statisticDataOptions, statisticReply ->
        {
            if (statisticReply.succeeded()) {

                JsonObject statisticResponse = (JsonObject) statisticReply.result().body();

                if (ReplyHandler.isReplyOk(statisticResponse))
                {
                    HTTPResponseHandler.configureOK(context, statisticResponse);
                }
                else
                {
                    HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError("Failed to retrieve statistic for project " + projectName));
                }
            }
        });

    }

    /**
     * Initiate load image files
     * PUT http://localhost:{port}/v2/imagefiles
     *
     * Example:
     * PUT http://localhost:{port}/v2/imagefiles
     */
    public void selectImageFile(RoutingContext context)
    {
        helper.checkIfDockerEnv(context);

        if(!imageFileSelector.isWindowOpen())
        {
            imageFileSelector.run();

        }

        HTTPResponseHandler.configureOK(context);
    }

    /**
     * Get load image files status
     * GET http://localhost:{port}/v2/imagefiles
     *
     * Example:
     * GET http://localhost:{port}/v2/imagefiles
     */
    public void selectImageFileStatus(RoutingContext context) {
        helper.checkIfDockerEnv(context);

        SelectionWindowStatus status = imageFileSelector.getWindowStatus();

        JsonObject jsonResponse = compileSelectionWindowResponse(status);

        if (status.equals(SelectionWindowStatus.WINDOW_CLOSE)) {
            jsonResponse.put(ParamConfig.getImgPathListParam(), imageFileSelector.getImageFilePathList());
            jsonResponse.put(ParamConfig.getImgDirectoryListParam(), imageFileSelector.getImageDirectoryList());
        }

        HTTPResponseHandler.configureOK(context, jsonResponse);
    }

    /**
     * Initiate add image files
     * PUT http://localhost:{port}/v2/:annotation_type/projects/:project_name/add
     *
     * Example:
     * PUT http://localhost:{port}/v2/bndbox/projects/vehicles/add
     */
    public void addImages(RoutingContext context) {

        AnnotationType type = AnnotationHandler.getTypeFromEndpoint(context.request().getParam(ParamConfig.getAnnotationTypeParam()));

        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());

        ProjectLoader loader = ProjectHandler.getProjectLoader(projectName, type);

        if(helper.checkIfProjectNull(context, loader, projectName)) return;

        File projectPath = loader.getProjectPath();
        List<String> fileNames = FileHandler.processFolder(projectPath, ImageHandler::isImageFileValid);

        log.info("Saving images to " + projectName + "......");

        context.request().bodyHandler( h ->
        {
            JsonObject request = Objects.requireNonNull(ConversionHandler.json2JSONObject(h.toJson()));
            JsonArray imageNameJsonArray = request.getJsonArray(ParamConfig.getImgNameListParam());
            JsonArray imageBase64JsonArray = request.getJsonArray(ParamConfig.getImgBase64ListParam());

            for(int i = 0; i < imageNameJsonArray.size(); i++)
            {
                try
                {
                    //decode image base64 string into image
                    byte[] decodedBytes = Base64.getDecoder().decode(imageBase64JsonArray.getString(i).split("base64,")[1]);
                    File imageFile = new File(projectPath.getAbsolutePath() + File.separator + imageNameJsonArray.getString(i));

                    if(!fileNames.contains(imageFile.getAbsolutePath()))
                    {
                        FileUtils.writeByteArrayToFile(imageFile, decodedBytes);
                    }
                    else
                    {
                        log.info(imageFile.getName() + " is exist in current folder");
                    }
                }
                catch (IOException e)
                {
                    log.info("Fail to convert Base64 String to Image file");
                    return;
                }
            }

        });

        loader.setFileSystemStatus(FileSystemStatus.ITERATING_FOLDER);

        JsonObject jsonObject = new JsonObject().put(ParamConfig.getProjectIdParam(), loader.getProjectId());

        DeliveryOptions reloadOps = new DeliveryOptions().addHeader(ParamConfig.getActionKeyword(), PortfolioDbQuery.getReloadProject());

        vertx.eventBus().request(PortfolioDbQuery.getQueue(), jsonObject, reloadOps, fetch ->
        {
            JsonObject response = (JsonObject) fetch.result().body();

            if (ReplyHandler.isReplyOk(response))
            {
                HTTPResponseHandler.configureOK(context);

            } else
            {
                HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError("Failed to add images to " + projectName));
            }
        });
    }

    /**
     * Get load status of project
     * GET http://localhost:{port}/v2/:annotation_type/projects/:project_name/addstatus
     *
     * Example:
     * GET http://localhost:{port}/v2/bndbox/projects/helloworld/addstatus
     *
     */
    public void addImagesStatus(RoutingContext context)
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
        }

        HTTPResponseHandler.configureOK(context, res);
    }

    /**
     * Initiate move image files and folder
     * PUT http://localhost:{port}/v2/:annotation_type/projects/:project_name/move
     *
     * Example:
     * PUT http://localhost:{port}/v2/bndbox/projects/vehicles/move
     */
    public void moveImages(RoutingContext context) {

        AnnotationType type = AnnotationHandler.getTypeFromEndpoint(context.request().getParam(ParamConfig.getAnnotationTypeParam()));

        String projectName = context.request().getParam(ParamConfig.getProjectNameParam());

        ProjectLoader loader = ProjectHandler.getProjectLoader(projectName, type);

        if(helper.checkIfProjectNull(context, loader, projectName)) return;

        File projectPath = loader.getProjectPath();
        List<String> currentFolderFileNames = FileHandler.processFolder(projectPath, ImageHandler::isImageFileValid);
        List<String> fileNames = currentFolderFileNames.stream().map(FilenameUtils::getName).collect(Collectors.toList());
        File[] filesList = projectPath.listFiles();
        List<String> folderNames = new ArrayList<>();
        List<String> folderList = Arrays.stream(Objects.requireNonNull(filesList)).map(File::getAbsolutePath).collect(Collectors.toList());

        for(File file : Objects.requireNonNull(filesList))
        {
            if(file.isDirectory())
            {
                folderNames.add(file.getName());
            }
        }

        context.request().bodyHandler( h ->
        {
            JsonObject request = Objects.requireNonNull(ConversionHandler.json2JSONObject(h.toJson()));
            Boolean modifyImageName = request.getBoolean(ParamConfig.getModifyImgNameParam());
            Boolean replaceImageName = request.getBoolean(ParamConfig.getReplaceImgNameParam());

            List<String> imageFilePathList = imageFileSelector.getImageFilePathList();
            List<String> imageDirectoryList = imageFileSelector.getImageDirectoryList();

            log.info("Moving selected images or folder to " + projectName + "......");

            String backUpFolderPath = projectPath.getParent() + File.separator + "Moved_Image_Backup" + File.separator;

            for (int i = 0; i < imageFilePathList.size(); i++)
            {
                try
                {
                    if(fileNames.contains(FilenameUtils.getName(imageFilePathList.get(i))))
                    {
                        ImageHandler.checkAndBackUpFolder(backUpFolderPath, imageFilePathList, currentFolderFileNames,
                                fileNames, projectPath, i, true);
                    }

                    if(!modifyImageName && replaceImageName)
                    {
                        String fileName = FilenameUtils.getName(imageFilePathList.get(i));
                        File deleteFile = new File(backUpFolderPath + fileName);
                        Files.delete(deleteFile.toPath());
                        FileUtils.moveFileToDirectory(new File(imageFilePathList.get(i)), projectPath, false);
                        log.info("Original image was replaced by selected image");
                    }

                    if(modifyImageName && !replaceImageName )
                    {
                        FileUtils.moveFileToDirectory(new File(imageFilePathList.get(i)), projectPath, false);
                        String fileName = FilenameUtils.getName(imageFilePathList.get(i));
                        String fileBaseName = FilenameUtils.getBaseName(imageFilePathList.get(i));
                        String fileExtension = FilenameUtils.getExtension(imageFilePathList.get(i));
                        File oldFile = new File(projectPath.getPath() + File.separator + fileName);
                        File newFile = new File(projectPath.getAbsolutePath() + File.separator
                                + fileBaseName + "_" + i + "." + fileExtension);
                        Files.move(newFile.toPath(), oldFile.toPath());
                        log.info("Selected image has been renamed to " + newFile.getName());
                    }

                    if(!modifyImageName && !replaceImageName)
                    {
                        FileUtils.moveFileToDirectory(new File(imageFilePathList.get(i)), projectPath, false);
                        log.info("Selected image has been moved to current project folder");
                    }

                }
                catch (IOException e)
                {
                    log.info("Fail to move selected images to current project folder");
                }
            }

            for (int j = 0; j < imageDirectoryList.size(); j++)
            {
                try {
                    if(folderNames.contains(FilenameUtils.getName(imageDirectoryList.get(j))))
                    {
                        ImageHandler.checkAndBackUpFolder(backUpFolderPath, imageDirectoryList, folderList,
                                folderNames, projectPath, j, false);
                    }

                    if(!modifyImageName && replaceImageName)
                    {
                        String folderName = FilenameUtils.getName(imageDirectoryList.get(j));
                        File deleteFolder = new File(backUpFolderPath + folderName);
                        FileUtils.deleteDirectory(deleteFolder);
                        FileUtils.moveDirectoryToDirectory(new File(imageDirectoryList.get(j)), projectPath, false);
                        log.info("The original folder was replaced by selected folder");
                    }

                    if(modifyImageName && !replaceImageName )
                    {
                        FileUtils.moveDirectoryToDirectory(new File(imageDirectoryList.get(j)), projectPath, false);
                        String folderBaseName = FilenameUtils.getBaseName(imageDirectoryList.get(j));
                        String folderModifyName = folderBaseName + "_" + j;
                        File oldFolder = new File(projectPath.getPath() + File.separator + folderBaseName);
                        File newFolder = new File(projectPath.getAbsolutePath() + File.separator + folderModifyName);
                        oldFolder.renameTo(newFolder);
                        log.info("Selected folder has been renamed to " + newFolder.getName());
                    }

                    if(!modifyImageName && !replaceImageName)
                    {
                        FileUtils.moveDirectoryToDirectory(new File(imageDirectoryList.get(j)), projectPath, false);
                        log.info("Selected folder has moved into current project folder");
                    }

                }
                catch (IOException e)
                {
                    log.info("Fail to move selected image folder to current project folder");
                }
            }
        });

        loader.setFileSystemStatus(FileSystemStatus.ITERATING_FOLDER);

        JsonObject jsonObject = new JsonObject().put(ParamConfig.getProjectIdParam(), loader.getProjectId());

        DeliveryOptions reloadOps = new DeliveryOptions().addHeader(ParamConfig.getActionKeyword(), PortfolioDbQuery.getReloadProject());

        vertx.eventBus().request(PortfolioDbQuery.getQueue(), jsonObject, reloadOps, fetch ->
        {
            JsonObject response = (JsonObject) fetch.result().body();

            if (ReplyHandler.isReplyOk(response))
            {
                HTTPResponseHandler.configureOK(context);

            } else
            {
                HTTPResponseHandler.configureOK(context, ReplyHandler.reportUserDefinedError("Failed to move selected images or folder to " + projectName));
            }
        });
    }

    /**
     * Get load status of project
     * GET http://localhost:{port}/v2/:annotation_type/projects/:project_name/movestatus
     *
     * Example:
     * GET http://localhost:{port}/v2/bndbox/projects/helloworld/movestatus
     *
     */
    public void moveImagesStatus(RoutingContext context)
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

    /**
     * Delete selected image and folder
     * PUT http://localhost:{port}/v2/deleteimagefiles
     *
     * Example:
     * PUT http://localhost:{port}/v2/deleteimagefiles
     *
     */
    public void deleteMoveImageAndFolder(RoutingContext context)
    {
        context.request().bodyHandler(h ->
        {
            JsonObject request = Objects.requireNonNull(ConversionHandler.json2JSONObject(h.toJson()));

            JsonArray imagePathList = request.getJsonArray(ParamConfig.getImgPathListParam());
            JsonArray imageDirectoryList = request.getJsonArray(ParamConfig.getImgDirectoryListParam());

            if(!imagePathList.isEmpty()) {
                for(int i = 0; i < imagePathList.size(); i++) {
                    imageFileSelector.getImageFilePathList().remove(imagePathList.getString(i));
                }
            }

            if(!imageDirectoryList.isEmpty()) {
                for(int i = 0; i < imageDirectoryList.size(); i++) {
                    imageFileSelector.getImageDirectoryList().remove(imageDirectoryList.getString(i));
                }
            }
        });

        HTTPResponseHandler.configureOK(context);

    }

}
