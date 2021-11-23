package ai.classifai.router.endpoint;

import ai.classifai.dto.api.body.MoveImageAndFolderBody;
import ai.classifai.dto.api.response.ImageAndFolderToProjectResponse;
import ai.classifai.loader.ProjectLoader;
import ai.classifai.ui.NativeUI;
import ai.classifai.ui.enums.ImageAndFolderToProjectStatus;
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

        log.info("Moving selected images or folder to " + projectName + "......");

        String backUpFolderPath = projectPath.getParent() + File.separator + "Moved_Image_Backup" + File.separator;

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

        ImageAndFolderToProjectResponse moveImageAndFolderResponse;

        int currentAddedImages = ImageHandler.getCurrentAddedImages();
        int totalImagesToBeAdded = ImageHandler.getTotalImagesToBeAdded();
        int currentAddedFolders = ImageHandler.getCurrentAddedFolders();
        int totalFoldersToBeAdded = ImageHandler.getTotalFoldersToBeAdded();

        if(currentAddedImages != 0 && totalImagesToBeAdded != 0 && currentAddedImages < totalImagesToBeAdded)
        {
            ImageAndFolderToProjectStatus addImageStatus = ImageAndFolderToProjectStatus.ADDING_IMAGES;

            moveImageAndFolderResponse = ImageAndFolderToProjectResponse.builder()
                    .message(ReplyHandler.SUCCESSFUL)
                    .addImageStatus(addImageStatus.ordinal())
                    .addImageMessage(addImageStatus.name())
                    .build();

            return moveImageAndFolderResponse;

        }

        if(currentAddedFolders != 0 && totalFoldersToBeAdded != 0 && currentAddedFolders < totalFoldersToBeAdded)
        {
            ImageAndFolderToProjectStatus addDirectoryStatus = ImageAndFolderToProjectStatus.ADDING_FOLDERS;

            moveImageAndFolderResponse = ImageAndFolderToProjectResponse.builder()
                    .message(ReplyHandler.SUCCESSFUL)
                    .addFolderStatus(addDirectoryStatus.ordinal())
                    .addFolderMessage(addDirectoryStatus.name())
                    .build();

            return moveImageAndFolderResponse;

        }

        if(totalImagesToBeAdded > 0 && currentAddedImages == totalImagesToBeAdded)
        {
            ImageAndFolderToProjectStatus addImageStatus = ImageAndFolderToProjectStatus.IMAGES_ADDED;

            moveImageAndFolderResponse = ImageAndFolderToProjectResponse.builder()
                    .message(ReplyHandler.SUCCESSFUL)
                    .addImageStatus(addImageStatus.ordinal())
                    .addImageMessage(addImageStatus.name())
                    .build();

            //zero back the amount
            ImageHandler.setCurrentAddedImages(0);
            ImageHandler.setTotalImagesToBeAdded(0);

            return moveImageAndFolderResponse;

        }

        if(totalFoldersToBeAdded > 0 && currentAddedFolders == totalFoldersToBeAdded)
        {
            ImageAndFolderToProjectStatus addDirectoryStatus = ImageAndFolderToProjectStatus.FOLDERS_ADDED;

            moveImageAndFolderResponse = ImageAndFolderToProjectResponse.builder()
                    .message(ReplyHandler.SUCCESSFUL)
                    .addFolderStatus(addDirectoryStatus.ordinal())
                    .addFolderMessage(addDirectoryStatus.name())
                    .build();

            //zero back the amount
            ImageHandler.setCurrentAddedFolders(0);
            ImageHandler.setTotalFoldersToBeAdded(0);

            return moveImageAndFolderResponse;

        }

        else
        {
            ImageAndFolderToProjectStatus addImageStatus = ImageAndFolderToProjectStatus.OPERATION_FAILED;
            ImageAndFolderToProjectStatus addDirectoryStatus = ImageAndFolderToProjectStatus.OPERATION_FAILED;

            moveImageAndFolderResponse = ImageAndFolderToProjectResponse.builder()
                    .message(ReplyHandler.FAILED)
                    .errorMessage("Add Images or Add Folder operation failed to perform")
                    .addImageStatus(addImageStatus.ordinal())
                    .addImageMessage(addImageStatus.name())
                    .addFolderStatus(addDirectoryStatus.ordinal())
                    .addFolderMessage(addDirectoryStatus.name())
                    .build();

            //zero back the amount
            ImageHandler.setCurrentAddedImages(0);
            ImageHandler.setTotalImagesToBeAdded(0);
            ImageHandler.setCurrentAddedFolders(0);
            ImageHandler.setTotalFoldersToBeAdded(0);
            log.info("Operation of adding selected images and folders to project " + projectName + " failed");

            return moveImageAndFolderResponse;

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
}
