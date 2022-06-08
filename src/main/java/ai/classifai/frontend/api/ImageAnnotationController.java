package ai.classifai.frontend.api;

import ai.classifai.core.dto.BoundingBoxDTO;
import ai.classifai.core.dto.properties.BoundingBoxProperties;
import ai.classifai.core.dto.properties.ImageProperties;
import ai.classifai.core.service.annotation.AnnotationService;
import ai.classifai.frontend.request.ImageAnnotationBody;
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
    private final AnnotationService<BoundingBoxDTO, BoundingBoxProperties, ImageProperties> annotationService;

    public ImageAnnotationController(AnnotationService<BoundingBoxDTO, BoundingBoxProperties, ImageProperties> annotationService) {
        this.annotationService = annotationService;
    }

    @POST
    @Path("/imglabel/bndbox")
    public void createAnnotation(ImageAnnotationBody imageAnnotationBody) {
        annotationService.setProperties(imageAnnotationBody.getImageProperties());
        annotationService.createAnnotation(imageAnnotationBody.getBoundingBoxProperties());
    }
}
