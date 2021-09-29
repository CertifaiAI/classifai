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
import ai.classifai.action.rename.RenameProjectData;
import ai.classifai.database.portfolio.PortfolioDB;
import ai.classifai.dto.api.reader.CreateProjectReader;
import ai.classifai.dto.api.reader.DeleteProjectDataReader;
import ai.classifai.dto.api.reader.ProjectStatusReader;
import ai.classifai.dto.api.reader.RenameDataReader;
import ai.classifai.dto.api.reader.body.CreateProjectBody;
import ai.classifai.dto.api.reader.body.DeleteProjectDataBody;
import ai.classifai.dto.api.reader.body.ProjectStatusBody;
import ai.classifai.dto.api.reader.body.RenameDataBody;
import ai.classifai.dto.api.response.*;
import ai.classifai.loader.ProjectLoader;
import ai.classifai.loader.ProjectLoaderStatus;
import ai.classifai.selector.project.LabelFileSelector;
import ai.classifai.selector.project.ProjectFolderSelector;
import ai.classifai.selector.project.ProjectImportSelector;
import ai.classifai.selector.status.FileSystemStatus;
import ai.classifai.selector.status.NewProjectStatus;
import ai.classifai.selector.status.SelectionWindowStatus;
import ai.classifai.util.collection.UuidGenerator;
import ai.classifai.util.http.ActionStatus;
import ai.classifai.util.http.HTTPResponseHandler;
import ai.classifai.util.message.ReplyHandler;
import ai.classifai.util.project.ProjectHandler;
import ai.classifai.util.project.ProjectInfra;
import ai.classifai.util.type.AnnotationHandler;
import ai.classifai.util.type.AnnotationType;
import com.zandero.rest.annotation.RequestReader;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
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
    @PUT
    @Path("/{annotation_type}/projects/{project_name}")
    @RequestReader(ProjectStatusReader.class)
    @Produces(MediaType.APPLICATION_JSON)
    public ActionStatus closeProjectState(@PathParam("annotation_type") String annotationType,
                                          @PathParam("project_name") String projectName,
                                          ProjectStatusBody requestBody)
    {
        AnnotationType type = AnnotationHandler.getTypeFromEndpoint(annotationType);
        String projectID = ProjectHandler.getProjectId(projectName, type.ordinal());

        if(helper.checkIfProjectNull(projectID)) {
            return ActionStatus.failDefault();
        }

        if(requestBody.getStatus().equals("closed"))
        {
            Objects.requireNonNull(ProjectHandler.getProjectLoader(projectID)).setIsLoadedFrontEndToggle(Boolean.FALSE);
        }
        else
        {
            throw new IllegalArgumentException("Request payload failed to satisfied the status of {\"status\": \"closed\"} for " + projectName + ". ");
        }

        return ActionStatus.ok();
    }

    /***
     * Star a project
     *
     * PUT http://localhost:{port}/:annotation_type/projects/:projectname/star
     */
    @PUT
    @Path("/{annotation_type}/projects/{project_name}/star")
    @RequestReader(ProjectStatusReader.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Future<ActionStatus> starProject(@PathParam("annotation_type") String annotationType,
                                            @PathParam("project_name") String projectName,
                                            ProjectStatusBody requestBody)
    {
        AnnotationType type = AnnotationHandler.getTypeFromEndpoint(annotationType);
        String projectID = ProjectHandler.getProjectId(projectName, type.ordinal());

        if(helper.checkIfProjectNull(projectID)) {
            return HTTPResponseHandler.nullProjectResponse();
        }

        Boolean isStarred = Boolean.parseBoolean(requestBody.getStatus());
        return portfolioDB.starProject(projectID, isStarred)
                .map(result -> {
                    ProjectLoader loader = Objects.requireNonNull(ProjectHandler.getProjectLoader(projectID));
                    loader.setIsProjectStarred(isStarred);
                    return ActionStatus.ok();
                })
                .otherwise(ActionStatus.failedWithMessage("Star project fail"));
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
    @PUT
    @Path("/v2/projects")
    @RequestReader(CreateProjectReader.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Future<ActionStatus> createProject(CreateProjectBody requestBody)
    {
        Promise<ActionStatus> promise = Promise.promise();
        try
        {
            String projectStatus = requestBody.getStatus().toUpperCase();

            if(projectStatus.equals(NewProjectStatus.CONFIG.name()))
            {
                projectImporter.run();
                promise.complete(ActionStatus.ok());
            }
            else if(projectStatus.equals(NewProjectStatus.RAW.name()))
            {
                promise.complete(createRawProject(requestBody));
            }
            else
            {
                String errorMessage = "Project status: " + projectStatus + " not recognizable. Expect " + NewProjectStatus.getParamList();

                promise.complete(ActionStatus.failedWithMessage(errorMessage));
            }
        }
        catch(Exception e)
        {
            String errorMessage = "Parameter of status with " + NewProjectStatus.getParamList() + " is compulsory in request body";

            promise.complete(ActionStatus.failedWithMessage(errorMessage));
        }

        return promise.future();
    }

    protected ActionStatus createRawProject(CreateProjectBody requestBody) throws IOException {
        String projectName = requestBody.getProjectName();

        String annotationName = requestBody.getAnnotationType();
        Integer annotationInt = AnnotationHandler.getType(annotationName).ordinal();

        if (ProjectHandler.isProjectNameUnique(projectName, annotationInt))
        {
            String projectPath = requestBody.getProjectPath();

            String labelPath = requestBody.getLabelFilePath();
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

            return ActionStatus.ok();
        }

        return ActionStatus.failedWithMessage("Project name exist: " + projectName);
    }

    /**
     * Create new project status
     * GET http://localhost:{port}/v2/:annotation_type/projects/:project_name
     *
     * Example:
     * GET http://localhost:{port}/v2/bndbox/projects/helloworld
     */
    @GET
    @Path("/v2/{annotation_type}/projects/{project_name}")
    @Produces(MediaType.APPLICATION_JSON)
    public FileSysStatusResponse createProjectStatus(@PathParam("annotation_type") String annotationType,
                                                     @PathParam("project_name") String projectName)
    {
        AnnotationType type = AnnotationHandler.getTypeFromEndpoint(annotationType);

        ProjectLoader loader = ProjectHandler.getProjectLoader(projectName, type);

        FileSystemStatus status = loader.getFileSystemStatus();

        FileSysStatusResponse response = FileSysStatusResponse.builder()
                .message(ReplyHandler.getSUCCESSFUL())
                .fileSystemStatus(status.ordinal())
                .fileSystemMessage(status.name())
                .build();

        if (status.equals(FileSystemStatus.DATABASE_UPDATED))
        {
            response.setUnsupportedImageList(loader.getUnsupportedImageList());
        }

        return response;
    }

    /**
     * Rename project
     * PUT http://localhost:{port}/v2/:annotation_type/projects/:project_name/rename/:new_project_name
     *
     * Example:
     * PUT http://localhost:{port}/v2/bndbox/projects/helloworld/rename/helloworldnewname
     *
     */
    @PUT
    @Path("/v2/{annotation_type}/projects/{project_name}/rename/{new_project_name}")
    @Produces(MediaType.APPLICATION_JSON)
    public Future<ActionStatus> renameProject(@PathParam("annotation_type") String annotationType,
                                              @PathParam("project_name") String projectName,
                                              @PathParam("new_project_name") String newProjectName)
    {
        AnnotationType type = AnnotationHandler.getTypeFromEndpoint(annotationType);
        ProjectLoader loader = ProjectHandler.getProjectLoader(projectName, type);

        if(helper.checkIfProjectNull(loader)) {
            return HTTPResponseHandler.nullProjectResponse();
        }

        if(ProjectHandler.checkValidProjectRename(newProjectName, type.ordinal()))
        {
            return portfolioDB.renameProject(loader.getProjectId(), newProjectName)
                    .map(result -> {
                        loader.setProjectName(newProjectName);
                        ProjectHandler.updateProjectNameInCache(loader.getProjectId(), loader, projectName);
                        log.debug("Rename to " + newProjectName + " success.");

                        return ActionStatus.ok();
                    })
                    .otherwise(ActionStatus.failedWithMessage("Failed to rename project " + projectName));
        }

        Promise<ActionStatus> promise = Promise.promise();
        promise.complete(ActionStatus.ok());

        return promise.future();
    }

    /**
     * Reload v2 project
     * PUT http://localhost:{port}/v2/:annotation_type/projects/:project_name/reload
     *
     * Example:
     * PUT http://localhost:{port}/v2/bndbox/projects/helloworld/reload
     *
     */
    @PUT
    @Path("/v2/{annotation_type}/projects/{project_name}/reload")
    @Produces(MediaType.APPLICATION_JSON)
    public Future<ActionStatus> reloadProject(@PathParam("annotation_type") String annotationType,
                                              @PathParam("project_name") String projectName)
    {
        AnnotationType type = AnnotationHandler.getTypeFromEndpoint(annotationType);

        log.info("Reloading project: " + projectName + " of annotation type: " + type.name());

        ProjectLoader loader = ProjectHandler.getProjectLoader(projectName, type);

        if(helper.checkIfProjectNull(loader)) {
            return HTTPResponseHandler.nullProjectResponse();
        }

        loader.setFileSystemStatus(FileSystemStatus.ITERATING_FOLDER);

        return portfolioDB.reloadProject(loader.getProjectId())
                .map(ActionStatus.ok())
                .otherwise(ActionStatus.failedWithMessage("Failed to reload project " + projectName));
    }

    /**
     * Get load status of project
     * GET http://localhost:{port}/v2/:annotation_type/projects/:project_name/reloadstatus
     *
     * Example:
     * GET http://localhost:{port}/v2/bndbox/projects/helloworld/reloadstatus
     *
     */
    @GET
    @Path("/v2/{annotation_type}/projects/{project_name}/reloadstatus")
    @Produces(MediaType.APPLICATION_JSON)
    public ReloadProjectStatus reloadProjectStatus(@PathParam("annotation_type") String annotationType,
                                                   @PathParam("project_name") String projectName)
    {
        AnnotationType type = AnnotationHandler.getTypeFromEndpoint(annotationType);
        ProjectLoader loader = ProjectHandler.getProjectLoader(projectName, type);

        if(helper.checkIfProjectNull(loader)) {
            return ReloadProjectStatus.builder()
                    .message(ReplyHandler.getFAILED())
                    .errorMessage("Project not exist")
                    .build();
        }

        FileSystemStatus fileSysStatus = loader.getFileSystemStatus();

        ReloadProjectStatus response = ReloadProjectStatus.builder()
                .message(ReplyHandler.getSUCCESSFUL())
                .fileSystemStatus(fileSysStatus.ordinal())
                .fileSystemMessage(fileSysStatus.name())
                .build();

        if(fileSysStatus.equals(FileSystemStatus.DATABASE_UPDATING))
        {
            response.setProgress(loader.getProgressUpdate());
        }
        else if(fileSysStatus.equals(FileSystemStatus.DATABASE_UPDATED))
        {
            response.setUuidAddList(loader.getReloadAdditionList());
            response.setUuidDeleteList(loader.getReloadDeletionList());
            response.setUnsupportedImageList(loader.getUnsupportedImageList());
        }

        return response;
    }

    /***
     * export a project to configuration file
     *
     * PUT http://localhost:{port}/v2/:annotation_type/projects/:project_name/export/:export_type
     */
    @PUT
    @Path("/v2/{annotation_type}/projects/{project_name}/export/{export_type}")
    @Produces(MediaType.APPLICATION_JSON)
    public Future<ActionStatus> exportProject(@PathParam("annotation_type") String annotationType,
                                              @PathParam("project_name") String projectName,
                                              @PathParam("export_type") String exportTypeVar)
    {
        AnnotationType type = AnnotationHandler.getTypeFromEndpoint(annotationType);

        String projectId = ProjectHandler.getProjectId(projectName, type.ordinal());

        if(helper.checkIfProjectNull(projectId)) {
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
    @Produces(MediaType.APPLICATION_JSON)
    public ExportStatusResponse getExportStatus()
    {
        ProjectExport.ProjectExportStatus exportStatus = ProjectExport.getExportStatus();

        ExportStatusResponse response = ExportStatusResponse.builder()
                .message(ReplyHandler.getSUCCESSFUL())
                .exportStatus(exportStatus.ordinal())
                .exportStatusMessage(exportStatus.name())
                .build();

        if(exportStatus.equals(ProjectExport.ProjectExportStatus.EXPORT_SUCCESS))
        {
            response.setProjectConfigPath(ProjectExport.getExportPath());
        }

       return response;
    }

    /**
     * Get import project status
     * GET http://localhost:{port}/v2/:annotation_type/projects/c
     *
     * Example:
     * GET http://localhost:{port}/v2/bndbox/projects/importstatus
     *
     */
    @GET
    @Path("/v2/{annotation_type}/projects/importstatus")
    @Produces(MediaType.APPLICATION_JSON)
    public FileSysStatusResponse getImportStatus()
    {
        FileSystemStatus fileSysStatus = ProjectImportSelector.getImportFileSystemStatus();

        FileSysStatusResponse response = FileSysStatusResponse.builder()
                .message(ReplyHandler.getSUCCESSFUL())
                .fileSystemStatus(fileSysStatus.ordinal())
                .fileSystemMessage(fileSysStatus.name())
                .build();

        if(fileSysStatus.equals(FileSystemStatus.DATABASE_UPDATED))
        {
            response.setProjectName(ProjectImportSelector.getProjectName());
        }

        return response;
    }

    /**
     * Initiate load label list
     * PUT http://localhost:{port}/v2/labelfiles
     *
     * Example:
     * PUT http://localhost:{port}/v2/labelfiles
     */
    @PUT
    @Path("/v2/labelfiles")
    @Produces(MediaType.APPLICATION_JSON)
    public ActionStatus selectLabelFile()
    {
        if(!labelFileSelector.isWindowOpen())
        {
            labelFileSelector.run();

        }

        return ActionStatus.ok();
    }

    /**
     * Get load label file status
     * GET http://localhost:{port}/v2/labelfiles
     *
     * Example:
     * GET http://localhost:{port}/v2/labelfiles
     */
    @GET
    @Path("/v2/labelfiles")
    @Produces(MediaType.APPLICATION_JSON)
    public SelectionStatusResponse selectLabelFileStatus()
    {
        SelectionWindowStatus status = labelFileSelector.getWindowStatus();

        SelectionStatusResponse response = SelectionStatusResponse.builder()
                .message(ReplyHandler.getSUCCESSFUL())
                .windowStatus(status.ordinal())
                .windowMessage(status.name())
                .build();

        if(status.equals(SelectionWindowStatus.WINDOW_CLOSE))
        {
            response.setLabelFilePath(labelFileSelector.getLabelFilePath());
        }

        return response;
    }


    /**
     * Open folder selector to choose project folder
     * PUT http://localhost:{port}/v2/folders
     *
     * Example:
     * PUT http://localhost:{port}/v2/folders
     */
    @PUT
    @Path("/v2/folders")
    @Produces(MediaType.APPLICATION_JSON)
    public ActionStatus selectProjectFolder()
    {
        if(!projectFolderSelector.isWindowOpen())
        {
            projectFolderSelector.run();
        }

        return ActionStatus.ok();
    }

    /**
     * Close/terminate classifai
     * PUT http://localhost:{port}/v2/close
     *
     * Example:
     * PUT http://localhost:{port}/v2/close
     */
    @PUT
    @Path("/v2/close")
    @Produces(MediaType.APPLICATION_JSON)
    public ActionStatus closeClassifai()
    {
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

        return ActionStatus.ok();
    }

    /**
     * Get status of choosing a project folder
     * GET http://localhost:{port}/v2/folders
     *
     * Example:
     * GET http://localhost:{port}/v2/folders
     */
    @GET
    @Path("/v2/folders")
    @Produces(MediaType.APPLICATION_JSON)
    public SelectionStatusResponse selectProjectFolderStatus()
    {
        SelectionWindowStatus status = projectFolderSelector.getWindowStatus();

        SelectionStatusResponse response = SelectionStatusResponse.builder()
                .message(ReplyHandler.getSUCCESSFUL())
                .windowStatus(status.ordinal())
                .windowMessage(status.name())
                .build();

        if(status.equals(SelectionWindowStatus.WINDOW_CLOSE))
        {
            response.setProjectPath(projectFolderSelector.getProjectFolderPath());
        }

        return response;
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
     * DELETE http://localhost:{port}/v2/bndbox/projects/helloworld/uuids
     */
    @DELETE
    @Path("/v2/{annotation_type}/projects/{project_name}/uuids")
    @RequestReader(DeleteProjectDataReader.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Future<DeleteProjectDataResponse> deleteProjectData(@PathParam("annotation_type") String annotationType,
                                                               @PathParam("project_name") String projectName,
                                                               DeleteProjectDataBody requestBody)
    {
        AnnotationType type = AnnotationHandler.getTypeFromEndpoint(annotationType);
        String projectID = ProjectHandler.getProjectId(projectName, type.ordinal());

        if(helper.checkIfProjectNull(projectID)) {
            return Future.succeededFuture(DeleteProjectDataResponse.builder()
                    .message(ReplyHandler.getFAILED())
                    .errorMessage("Project not exist")
                    .build());
        }

        return portfolioDB.deleteProjectData(projectID, requestBody.getUuidList(), requestBody.getImgPathList())
                .map(result -> DeleteProjectDataResponse.builder()
                        .message(ReplyHandler.getSUCCESSFUL())
                        .uuidList(result)
                        .build())
                .otherwise(DeleteProjectDataResponse.builder()
                        .message(ReplyHandler.getFAILED())
                        .errorMessage("Delete project data fail")
                        .build());

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
    @PUT
    @Path("/v2/{annotation_type}/projects/{project_name}/imgsrc/rename")
    @RequestReader(RenameDataReader.class)
    @Produces(MediaType.APPLICATION_JSON)
    public Future<RenameDataResponse> renameData(@PathParam("annotation_type") String annotationType,
                                                 @PathParam("project_name") String projectName,
                                                 RenameDataBody requestBody)
    {
        AnnotationType type = AnnotationHandler.getTypeFromEndpoint(annotationType);
        String projectId = ProjectHandler.getProjectId(projectName, type.ordinal());

        return portfolioDB.renameData(projectId, requestBody.getUuid(), requestBody.getNewFilename())
                .map(result -> RenameDataResponse.builder()
                        .message(ReplyHandler.getSUCCESSFUL())
                        .imgPath(result)
                        .build())
                .otherwise(cause -> RenameProjectData.reportRenameError(cause.getMessage()));
    }
}
