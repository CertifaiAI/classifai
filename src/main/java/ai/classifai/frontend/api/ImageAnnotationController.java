package ai.classifai.frontend.api;

import ai.classifai.frontend.request.ImageAnnotationBody;
import ai.classifai.core.services.project.ProjectService;
import ai.classifai.core.services.project.ProjectServiceImpl;
import ai.classifai.backend.repository.entity.annotation.ImageBoundingBoxAnnotation;
import ai.classifai.core.services.project.ProjectRepository;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Slf4j
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ImageAnnotationController {
    private ProjectService projectService;

    public ImageAnnotationController(ProjectRepository projectRepository) {
        this.projectService = new ProjectServiceImpl(projectRepository);
    }

    @POST
    @Path("/imglabel/bndbox")
    public void createAnnotation(ImageAnnotationBody imageAnnotationBody) {
        ImageBoundingBoxAnnotation imageBoundingBoxAnnotation =
                new ImageBoundingBoxAnnotation(imageAnnotationBody.getImageProperties());

        imageBoundingBoxAnnotation.createAnnotation(imageAnnotationBody.getBoundingBoxProperties());
        projectService.createAnnotation(imageBoundingBoxAnnotation);
    }
}
