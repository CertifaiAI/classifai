package ai.classifai.router.endpoint;

import ai.classifai.dto.api.body.MoveImageAndFolderBody;
import ai.classifai.dto.api.response.ImageAndFolderToProjectResponse;
import ai.classifai.loader.ProjectLoader;
import ai.classifai.ui.NativeUI;
import ai.classifai.ui.enums.ImageAndFolderToProjectStatus;
import ai.classifai.util.ParamConfig;
import ai.classifai.util.data.ImageHandler;
import ai.classifai.util.http.ActionStatus;
import ai.classifai.util.http.HTTPResponseHandler;
import ai.classifai.util.message.ReplyHandler;
import ai.classifai.util.project.ProjectHandler;
import ai.classifai.util.type.AnnotationType;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MoveImageAndFolderEndpoint {

    private final ProjectHandler projectHandler;
    private final NativeUI ui;

    public MoveImageAndFolderEndpoint(ProjectHandler projectHandler, NativeUI ui) {
        this.projectHandler = projectHandler;
        this.ui = ui;
    }

    /**
     * Initiate move image files and folder
     * PUT http://localhost:{port}/v2/:annotation_type/projects/:project_name/move
     *
     * Example:
     * PUT http://localhost:{port}/v2/bndbox/projects/vehicles/move
     */
    @PUT
    @Path("/v2/{annotation_type}/projects/{project_name}/move")
    public Future<ActionStatus> moveImages(@PathParam("annotation_type") String annotationType,
                                           @PathParam("project_name") String projectName,
                                           MoveImageAndFolderBody moveImageAndFolderBody)
    {

        AnnotationType type = AnnotationType.getTypeFromEndpoint(annotationType);

        ProjectLoader loader = projectHandler.getProjectLoader(projectName, type);

        if(loader == null) {
            return HTTPResponseHandler.nullProjectResponse();
        }

        File projectPath = loader.getProjectPath();

        Boolean modifyImageOrFolderName = moveImageAndFolderBody.getModifyImageOrFolderName();
        Boolean replaceImageOrFolder = moveImageAndFolderBody.getReplaceImageOrFolder();

        List<String> imageFilePathList = ui.getImagePathList();
        List<String> imageDirectoryList = ui.getImageDirectoryList();

        log.info("Moving selected images or folder to project folder " + projectName + "......");

        String backUpFolderPath = Paths.get(projectPath.getAbsolutePath(), ParamConfig.getDeleteDataFolderName()).toString();

        try
        {
            ImageHandler.moveImageToProjectFolder(imageFilePathList, modifyImageOrFolderName, replaceImageOrFolder,
                    backUpFolderPath, projectPath);
        }
        catch (IOException e)
        {
            log.info("Fail to move selected images to " + projectPath.getName());
        }

        try
        {
            ImageHandler.moveImageFolderToProjectFolder(imageDirectoryList, modifyImageOrFolderName, replaceImageOrFolder,
                    backUpFolderPath, projectPath);
        }
        catch (IOException e)
        {
            log.info("Fail to move selected image folders to " + projectPath.getName());
        }

        Promise<ActionStatus> promise = Promise.promise();
        promise.complete(ActionStatus.ok());

        return promise.future();
    }

    /**
     * Get load status of project
     * GET http://localhost:{port}/v2/:annotation_type/projects/:project_name/movestatus
     *
     * Example:
     * GET http://localhost:{port}/v2/bndbox/projects/helloworld/movestatus
     *
     */
    @GET
    @Path("/v2/{annotation_type}/projects/{project_name}/movestatus")
    public ImageAndFolderToProjectResponse moveImagesStatus(@PathParam("annotation_type") String annotationType,
                                                            @PathParam("project_name") String projectName)
    {
        AnnotationType type = AnnotationType.getTypeFromEndpoint(annotationType);

        ProjectLoader loader = projectHandler.getProjectLoader(projectName, type);

        if(loader == null) {
            return ImageAndFolderToProjectResponse.builder()
                    .message(ReplyHandler.FAILED)
                    .errorMessage("Project not exist")
                    .build();
        }

        ImageAndFolderToProjectResponse moveImageResponse = null;
        ImageAndFolderToProjectResponse moveFolderResponse = null;

        int currentAddedImages = ImageHandler.getCurrentAddedImages();
        int totalImagesToBeAdded = ImageHandler.getTotalImagesToBeAdded();
        int currentAddedFolders = ImageHandler.getCurrentAddedFolders();
        int totalFoldersToBeAdded = ImageHandler.getTotalFoldersToBeAdded();

        if(currentAddedImages < totalImagesToBeAdded || currentAddedFolders < totalFoldersToBeAdded) {

            if (currentAddedImages != 0 && totalImagesToBeAdded != 0 && currentAddedImages < totalImagesToBeAdded) {
                ImageAndFolderToProjectStatus addImageStatus = ImageAndFolderToProjectStatus.ADDING_IMAGES;

                moveImageResponse = ImageAndFolderToProjectResponse.builder()
                        .message(ReplyHandler.SUCCESSFUL)
                        .addImageStatus(addImageStatus.ordinal())
                        .addImageMessage(addImageStatus.name())
                        .build();
            }

            if (currentAddedFolders != 0 && totalFoldersToBeAdded != 0 && currentAddedFolders < totalFoldersToBeAdded) {
                ImageAndFolderToProjectStatus addDirectoryStatus = ImageAndFolderToProjectStatus.ADDING_FOLDERS;

                moveFolderResponse = ImageAndFolderToProjectResponse.builder()
                        .message(ReplyHandler.SUCCESSFUL)
                        .addFolderStatus(addDirectoryStatus.ordinal())
                        .addFolderMessage(addDirectoryStatus.name())
                        .build();

            }

            return combineResponse(moveImageResponse, totalImagesToBeAdded, moveFolderResponse, totalFoldersToBeAdded);
        }

        if(currentAddedImages == totalImagesToBeAdded || currentAddedFolders == totalFoldersToBeAdded)
        {
            if(currentAddedImages == totalImagesToBeAdded)
            {
                ImageAndFolderToProjectStatus addImageStatus = ImageAndFolderToProjectStatus.IMAGES_ADDED;

                moveImageResponse = ImageAndFolderToProjectResponse.builder()
                        .message(ReplyHandler.SUCCESSFUL)
                        .addImageStatus(addImageStatus.ordinal())
                        .addImageMessage(addImageStatus.name())
                        .build();

                //zero back the amount
                ImageHandler.setCurrentAddedImages(0);
                ImageHandler.setTotalImagesToBeAdded(0);
                ui.clearImagePathList();
            }

            if(currentAddedFolders == totalFoldersToBeAdded)
            {
                ImageAndFolderToProjectStatus addDirectoryStatus = ImageAndFolderToProjectStatus.FOLDERS_ADDED;

                moveFolderResponse = ImageAndFolderToProjectResponse.builder()
                        .message(ReplyHandler.SUCCESSFUL)
                        .addFolderStatus(addDirectoryStatus.ordinal())
                        .addFolderMessage(addDirectoryStatus.name())
                        .build();

                //zero back the amount
                ImageHandler.setCurrentAddedFolders(0);
                ImageHandler.setTotalFoldersToBeAdded(0);
                ui.clearImageDirectoryList();
            }

            return combineResponse(moveImageResponse, totalImagesToBeAdded, moveFolderResponse, totalFoldersToBeAdded);
        }

        else
        {
            ImageAndFolderToProjectStatus addImageStatus;
            ImageAndFolderToProjectStatus addDirectoryStatus;

            if(currentAddedImages == 0) {
                addImageStatus = ImageAndFolderToProjectStatus.OPERATION_FAILED;

                moveImageResponse = ImageAndFolderToProjectResponse.builder()
                        .message(ReplyHandler.FAILED)
                        .errorMessage("Move Images operation failed to perform")
                        .addImageStatus(addImageStatus.ordinal())
                        .addImageMessage(addImageStatus.name())
                        .build();
            }

            if(currentAddedFolders == 0) {
                addDirectoryStatus = ImageAndFolderToProjectStatus.OPERATION_FAILED;

                moveFolderResponse = ImageAndFolderToProjectResponse.builder()
                        .message(ReplyHandler.FAILED)
                        .errorMessage("Move Folder operation failed to perform")
                        .addFolderStatus(addDirectoryStatus.ordinal())
                        .addFolderMessage(addDirectoryStatus.name())
                        .build();
            }

            //zero back the amount
            ImageHandler.setCurrentAddedImages(0);
            ImageHandler.setTotalImagesToBeAdded(0);
            ImageHandler.setCurrentAddedFolders(0);
            ImageHandler.setTotalFoldersToBeAdded(0);
            ui.clearImagePathList();
            ui.clearImageDirectoryList();
            log.info("Operation of adding selected images and folders to project " + projectName + " failed");

            return combineFailResponse(moveImageResponse, currentAddedImages, moveFolderResponse, currentAddedFolders);
        }
    }

    /**
     * Delete selected image and folder
     * PUT http://localhost:{port}/v2/deleteimagefiles
     *
     * Example:
     * PUT http://localhost:{port}/v2/deleteimagefiles
     *
     */
    @PUT
    @Path("/v2/deleteimagefiles")
    public ActionStatus deleteMoveImageAndFolder(MoveImageAndFolderBody moveImageAndFolderBody)
    {
        List<String> imagePathList = moveImageAndFolderBody.getImagePathList();
        List<String> imageDirectoryList = moveImageAndFolderBody.getImageDirectoryList();

        if(!imagePathList.isEmpty()) {
            for (String imagePath : imagePathList) {
                ui.getImagePathList().remove(imagePath);
            }
        }

        if(!imageDirectoryList.isEmpty()) {
            for (String directory : imageDirectoryList) {
                ui.getImageDirectoryList().remove(directory);
            }
        }

        return ActionStatus.ok();

    }

    private ImageAndFolderToProjectResponse combineResponse(ImageAndFolderToProjectResponse moveImageResponse, Integer currentAddedImages,
                               ImageAndFolderToProjectResponse moveFolderResponse, Integer currentAddedFolders)
    {
        ImageAndFolderToProjectResponse imageAndFolderToProjectResponse = null;

        if(currentAddedImages != 0 && currentAddedFolders == 0) {
            imageAndFolderToProjectResponse = ImageAndFolderToProjectResponse.builder()
                    .message(moveImageResponse.getMessage())
                    .addImageStatus(moveImageResponse.getAddImageStatus())
                    .addImageMessage(moveImageResponse.getAddImageMessage())
                    .build();
        }

        else if (currentAddedImages == 0 && currentAddedFolders != 0) {
            imageAndFolderToProjectResponse = ImageAndFolderToProjectResponse.builder()
                    .message(moveFolderResponse.getMessage())
                    .addFolderStatus(moveFolderResponse.getAddFolderStatus())
                    .addFolderMessage(moveFolderResponse.getAddFolderMessage())
                    .build();
        }

        else if (currentAddedImages != 0) {
            imageAndFolderToProjectResponse = ImageAndFolderToProjectResponse.builder()
                    .message(moveFolderResponse.getMessage())
                    .addImageStatus(moveImageResponse.getAddImageStatus())
                    .addImageMessage(moveImageResponse.getAddImageMessage())
                    .addFolderStatus(moveFolderResponse.getAddFolderStatus())
                    .addFolderMessage(moveFolderResponse.getAddFolderMessage())
                    .build();
        }

        return imageAndFolderToProjectResponse;
    }

    private ImageAndFolderToProjectResponse combineFailResponse(ImageAndFolderToProjectResponse moveImageResponse, Integer currentAddedImages,
                                                            ImageAndFolderToProjectResponse moveFolderResponse, Integer currentAddedFolders)
    {
        ImageAndFolderToProjectResponse failResponse = null;

        if(currentAddedImages == 0 && currentAddedFolders == 0) {
            failResponse = ImageAndFolderToProjectResponse.builder()
                    .message(moveImageResponse.getMessage())
                    .errorMessage("Move Images and Folder operation failed to perform")
                    .addImageStatus(moveImageResponse.getAddImageStatus())
                    .addImageMessage(moveImageResponse.getAddImageMessage())
                    .addFolderStatus(moveFolderResponse.getAddFolderStatus())
                    .addFolderMessage(moveFolderResponse.getAddFolderMessage())
                    .build();
        }

        else if (currentAddedFolders == 0) {
            failResponse = ImageAndFolderToProjectResponse.builder()
                    .message(moveImageResponse.getMessage())
                    .errorMessage("Move Images operation failed to perform")
                    .addImageStatus(moveImageResponse.getAddImageStatus())
                    .addImageMessage(moveImageResponse.getAddImageMessage())
                    .build();
        }

        else if (currentAddedImages == 0) {
            failResponse = ImageAndFolderToProjectResponse.builder()
                    .message(moveImageResponse.getMessage())
                    .errorMessage("Move Folders operation failed to perform")
                    .addFolderStatus(moveFolderResponse.getAddFolderStatus())
                    .addFolderMessage(moveFolderResponse.getAddFolderMessage())
                    .build();
        }

        return failResponse;
    }
}
