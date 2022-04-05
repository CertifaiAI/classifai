package ai.classifai.router.endpoint;

import ai.classifai.action.LabelListImport;
import ai.classifai.database.annotation.AnnotationDB;
import ai.classifai.database.portfolio.PortfolioDB;
import ai.classifai.dto.api.body.CreateProjectBody;
import ai.classifai.dto.api.response.FileSysStatusResponse;
import ai.classifai.dto.api.response.LoadingStatusResponse;
import ai.classifai.dto.api.response.SelectionStatusResponse;
import ai.classifai.loader.ProjectLoader;
import ai.classifai.loader.ProjectLoaderStatus;
import ai.classifai.ui.NativeUI;
import ai.classifai.ui.enums.FileSystemStatus;
import ai.classifai.ui.enums.NewProjectStatus;
import ai.classifai.ui.enums.SelectionWindowStatus;
import ai.classifai.util.collection.UuidGenerator;
import ai.classifai.util.http.ActionStatus;
import ai.classifai.util.http.HTTPResponseHandler;
import ai.classifai.util.message.ReplyHandler;
import ai.classifai.util.project.ProjectHandler;
import ai.classifai.util.project.ProjectInfra;
import ai.classifai.util.type.AnnotationType;
import com.opencsv.exceptions.CsvException;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.xml.sax.SAXException;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Classifai v1 endpoints
 *
 * @author devenyantis
 */
@Slf4j
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ProjectEndpoint {
    private final PortfolioDB portfolioDB;
    private final NativeUI ui;
    private final ProjectHandler projectHandler;
    private final AnnotationDB annotationDB;

    public ProjectEndpoint(PortfolioDB portfolioDB, AnnotationDB annotationDB, NativeUI ui, ProjectHandler projectHandler) {
        this.portfolioDB = portfolioDB;
        this.ui = ui;
        this.projectHandler = projectHandler;
        this.annotationDB = annotationDB;
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
    public Future<ActionStatus> createProject(CreateProjectBody requestBody)
    {
        Promise<ActionStatus> promise = Promise.promise();
        try
        {
            String projectStatus = requestBody.getStatus().toUpperCase();

            if(projectStatus.equals(NewProjectStatus.CONFIG.name()))
            {
                ui.showProjectImportSelector();
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

    /**
     * Get import project status
     * GET http://localhost:{port}/v2/:annotation_type/projects/importstatus
     *
     * Example:
     * GET http://localhost:{port}/v2/bndbox/projects/importstatus
     *
     */
    @GET
    @Path("/v2/{annotation_type}/projects/importstatus")
    public FileSysStatusResponse getImportStatus()
    {
        FileSystemStatus fileSysStatus = ui.getProjectImportStatus();

        FileSysStatusResponse response = FileSysStatusResponse.builder()
                .message(ReplyHandler.SUCCESSFUL)
                .fileSystemStatus(fileSysStatus.ordinal())
                .fileSystemMessage(fileSysStatus.name())
                .build();

        if(fileSysStatus.equals(FileSystemStatus.DATABASE_UPDATED))
        {
            response.setProjectName(ui.getImportedProjectName());
        }

        return response;
    }

    protected ActionStatus createRawProject(CreateProjectBody requestBody) throws IOException {
        String projectName = requestBody.getProjectName();
        String annotationName = requestBody.getAnnotationType();
        Integer annotationInt = AnnotationType.get(annotationName).ordinal();
        String projectPath = requestBody.getProjectPath();
        String labelPath = requestBody.getLabelFilePath();
        List<String> labelList = new LabelListImport(new File(labelPath)).getValidLabelList();

        if (projectHandler.isProjectNameUnique(projectName, annotationInt))
        {
            switch (annotationInt) {
                case 0, 1 -> {
                    ProjectLoader loader = ProjectLoader.builder()
                            .projectId(UuidGenerator.generateUuid())
                            .projectName(projectName)
                            .annotationType(annotationInt)
                            .projectPath(new File(projectPath))
                            .labelList(labelList)
                            .projectLoaderStatus(ProjectLoaderStatus.LOADED)
                            .projectInfra(ProjectInfra.ON_PREMISE)
                            .fileSystemStatus(FileSystemStatus.ITERATING_FOLDER)
                            .portfolioDB(portfolioDB)
                            .annotationDB(annotationDB)
                            .build();
                    projectHandler.loadProjectLoader(loader);
                    loader.initFolderIteration();
                }
                case 2 -> {
                    String tabularFilePath = requestBody.getTabularFilePath();

                    ProjectLoader loader = ProjectLoader.builder()
                            .projectId(UuidGenerator.generateUuid())
                            .projectName(projectName)
                            .annotationType(annotationInt)
                            .projectPath(new File(tabularFilePath).getParentFile())
                            .tabularFilePath(new File(tabularFilePath))
                            .labelList(labelList)
                            .projectLoaderStatus(ProjectLoaderStatus.LOADED)
                            .projectInfra(ProjectInfra.ON_PREMISE)
                            .fileSystemStatus(FileSystemStatus.ITERATING_FOLDER)
                            .portfolioDB(portfolioDB)
                            .annotationDB(annotationDB)
                            .build();
                    projectHandler.loadProjectLoader(loader);

                    try {
                        String fileExtension = FilenameUtils.getExtension(tabularFilePath);
                        switch(fileExtension) {
                            case "xlsx", "xls" -> loader.parseExcelFile(tabularFilePath, fileExtension);
                            case "csv" ->  loader.parseCsvFile(tabularFilePath);
                            default -> throw new IllegalStateException("Unsupported file extension: " + fileExtension);
                        }
                    } catch (CsvException | OpenXML4JException e) {
                        log.info("Cannot parse the csv file");
                    } catch (SAXException | ParserConfigurationException e) {
                        log.info("File parse error. The selected file is not csv or excel file");
                    }
                }
            }

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
    public FileSysStatusResponse createProjectStatus(@PathParam("annotation_type") String annotationType,
                                                     @PathParam("project_name") String projectName)
    {
        AnnotationType type = AnnotationType.getTypeFromEndpoint(annotationType);

        ProjectLoader loader = projectHandler.getProjectLoader(projectName, type);

        if(loader == null) {
            return FileSysStatusResponse.builder()
                    .message(ReplyHandler.FAILED)
                    .errorMessage("Project not exist: " + projectName)
                    .build();
        }

        FileSystemStatus status = loader.getFileSystemStatus();

        FileSysStatusResponse response = FileSysStatusResponse.builder()
                .message(ReplyHandler.SUCCESSFUL)
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
     * Load existing project from the bounding box database
     *
     * GET http://localhost:{port}/:annotation_type/projects/:project_name
     *
     * Example:
     * GET http://localhost:{port}/bndbox/projects/helloworld
     *
     */
    @GET
    @Path("/{annotation_type}/projects/{project_name}")
    public Future<ActionStatus> loadProject(@PathParam("annotation_type") String annotationType,
                                            @PathParam("project_name") String projectName)
    {
        AnnotationType type = AnnotationType.getTypeFromEndpoint(annotationType);

        ProjectLoader loader = projectHandler.getProjectLoader(projectName, type);
        Promise<ActionStatus> promise = Promise.promise();

        if(loader == null) {
            return HTTPResponseHandler.nullProjectResponse();
        }

        loader.toggleFrontEndLoaderParam(); //if project is_new = true, change to false since loading the project

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
    @Path("/{annotation_type}/projects/{project_name}/loadingstatus")
    public LoadingStatusResponse loadProjectStatus(@PathParam("annotation_type") String annotationType,
                                                   @PathParam("project_name") String projectName)
    {
        AnnotationType type = AnnotationType.getTypeFromEndpoint(annotationType);
        ProjectLoader projectLoader = projectHandler.getProjectLoader(projectName, type);

        if (projectLoader == null) {
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
     * Delete project
     *
     * DELETE http://localhost:{port}/:annotation_type/projects/:project_name
     *
     * Example:
     * DELETE http://localhost:{port}/bndbox/projects/helloworld
     *
     */
    @DELETE
    @Path("/{annotation_type}/projects/{project_name}")
    public Future<ActionStatus> deleteProject(@PathParam("annotation_type") String annotationType,
                                              @PathParam("project_name") String projectName)
    {
        AnnotationType type = AnnotationType.getTypeFromEndpoint(annotationType);
        String projectID = projectHandler.getProjectId(projectName, type.ordinal());

        if(projectID == null) {
            return HTTPResponseHandler.nullProjectResponse();
        }

        String errorMessage = "Failure in delete project name: " + projectName + " for " + type.name();

        return portfolioDB.deleteProjectFromPortfolioDb(projectID)
                .compose(result -> portfolioDB.deleteProjectFromAnnotationDb(projectID))
                .map(result -> {
                    projectHandler.deleteProjectFromCache(projectID);
                    return ActionStatus.ok();
                })
                .onFailure(cause -> ActionStatus.failedWithMessage(errorMessage));
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
    public ActionStatus selectLabelFile()
    {
        if(!ui.isLabelFileSelectorOpen())
        {
            ui.showLabelFileSelector();
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
    public SelectionStatusResponse selectLabelFileStatus()
    {
        SelectionWindowStatus status = ui.getLabelFileSelectorWindowStatus();

        SelectionStatusResponse response = SelectionStatusResponse.builder()
                .message(ReplyHandler.SUCCESSFUL)
                .windowStatus(status.ordinal())
                .windowMessage(status.name())
                .build();

        if(status.equals(SelectionWindowStatus.WINDOW_CLOSE))
        {
            response.setLabelFilePath(ui.getLabelFileSelectedPath());
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
    public ActionStatus selectProjectFolder()
    {
        if(!ui.isProjectFolderSelectorOpen())
        {
            ui.showProjectFolderSelector();
        }

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
    public SelectionStatusResponse selectProjectFolderStatus()
    {
        SelectionWindowStatus status = ui.getProjectFolderSelectorWindowStatus();

        SelectionStatusResponse response = SelectionStatusResponse.builder()
                .message(ReplyHandler.SUCCESSFUL)
                .windowStatus(status.ordinal())
                .windowMessage(status.name())
                .build();

        if(status.equals(SelectionWindowStatus.WINDOW_CLOSE))
        {
            response.setProjectPath(ui.getProjectFolderSelectedPath());
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
    @Path("/v2/tabularfile")
    public ActionStatus selectTabularFile()
    {
        if(!ui.isTabularFileSelectorOpen())
        {
            ui.showTabularFileSelector();
        }

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
    @Path("/v2/tabularfile")
    public SelectionStatusResponse selectTabularFileStatus()
    {
        SelectionWindowStatus status = ui.getTabularFileSelectorWindowStatus();

        SelectionStatusResponse response = SelectionStatusResponse.builder()
                .message(ReplyHandler.SUCCESSFUL)
                .windowStatus(status.ordinal())
                .windowMessage(status.name())
                .build();

        if(status.equals(SelectionWindowStatus.WINDOW_CLOSE))
        {
            response.setTabularFilePath(ui.getTabularFileSelectedPath());
        }

        return response;
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

}
