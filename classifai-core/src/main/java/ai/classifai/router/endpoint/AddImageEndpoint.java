package ai.classifai.router.endpoint;

import ai.classifai.dto.api.body.AddImageBody;
import ai.classifai.dto.api.response.ImageAndFolderToProjectResponse;
import ai.classifai.loader.ProjectLoader;
import ai.classifai.ui.enums.ImageAndFolderToProjectStatus;
import ai.classifai.util.data.FileHandler;
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
import java.util.List;

@Slf4j
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AddImageEndpoint {

    private final ProjectHandler projectHandler;

    public AddImageEndpoint(ProjectHandler projectHandler) {
        this.projectHandler = projectHandler;
    }

    /**
     * Initiate add image files
     * PUT http://localhost:{port}/v2/:annotation_type/projects/:project_name/add
     *
     * Example:
     * PUT http://localhost:{port}/v2/bndbox/projects/vehicles/add
     */
    @PUT
    @Path("/v2/{annotation_type}/projects/{project_name}/add")
    public Future<ActionStatus> addImage(@PathParam("annotation_type") String annotationType,
                                         @PathParam("project_name") String projectName, AddImageBody addImageBody)
    {
        AnnotationType type = AnnotationType.getTypeFromEndpoint(annotationType);

        ProjectLoader loader = projectHandler.getProjectLoader(projectName, type);

        if(loader == null) {
            return HTTPResponseHandler.nullProjectResponse();
        }

        File projectPath = loader.getProjectPath();
        List<String> fileNames = FileHandler.processFolder(projectPath, ImageHandler::isImageFileValid);

        log.info("Saving images to " + projectName + "......");

        ImageHandler imageHandler = new ImageHandler();
        imageHandler.addImageToProjectFolder(addImageBody.getImgNameList(), addImageBody.getImgBase64List(), projectPath, fileNames);

        Promise<ActionStatus> promise = Promise.promise();
        promise.complete(ActionStatus.ok());

        return promise.future();
    }

    /**
     * Get load status of project
     * GET http://localhost:{port}/v2/:annotation_type/projects/:project_name/addstatus
     *
     * Example:
     * GET http://localhost:{port}/v2/bndbox/projects/helloworld/addstatus
     *
     */
    @GET
    @Path("/v2/{annotation_type}/projects/{project_name}/addstatus")
    public ImageAndFolderToProjectResponse addImagesStatus(@PathParam("annotation_type") String annotationType,
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

        ImageHandler imageHandler = new ImageHandler();
        int totalImagesToBeAdded = imageHandler.getTotalImagesToBeAdded();
        int currentAddedImages = imageHandler.getCurrentAddedImages();

        ImageAndFolderToProjectResponse addImageResponse;

        if(currentAddedImages == totalImagesToBeAdded)
        {
            ImageAndFolderToProjectStatus status = ImageAndFolderToProjectStatus.IMAGES_ADDED;

            addImageResponse = ImageAndFolderToProjectResponse.builder()
                    .message(ReplyHandler.SUCCESSFUL)
                    .addImageStatus(status.ordinal())
                    .addImageMessage(status.name())
                    .build();

            imageHandler.setCurrentAddedImages(0);
            imageHandler.setTotalImagesToBeAdded(0);
        }

        else
        {
            ImageAndFolderToProjectStatus status = ImageAndFolderToProjectStatus.OPERATION_FAILED;

            addImageResponse = ImageAndFolderToProjectResponse.builder()
                    .message(ReplyHandler.SUCCESSFUL)
                    .addImageStatus(status.ordinal())
                    .addImageMessage(status.name())
                    .build();

            imageHandler.setCurrentAddedImages(0);
            imageHandler.setTotalImagesToBeAdded(0);
            log.info("Operation of adding selected images to project " + projectName + " failed");
        }

        return addImageResponse;
    }


}
