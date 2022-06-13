package ai.classifai.frontend.api;

import ai.classifai.core.dto.BoundingBoxDTO;
import ai.classifai.core.dto.SegmentationDTO;
import ai.classifai.core.dto.properties.ImageProperties;
import ai.classifai.core.service.annotation.AnnotationService;
import ai.classifai.frontend.request.ImageAnnotationBody;
import ai.classifai.frontend.response.ActionStatus;
import io.vertx.core.Future;
import lombok.extern.slf4j.Slf4j;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

@Slf4j
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ImageAnnotationController {

    private final AnnotationService<BoundingBoxDTO, ImageProperties> boundingBoxService;
    private final AnnotationService<SegmentationDTO, ImageProperties> segmentationService;

    public ImageAnnotationController(AnnotationService<BoundingBoxDTO, ImageProperties> boundingBoxService,
                                     AnnotationService<SegmentationDTO, ImageProperties> segmentationService) {
        this.boundingBoxService = boundingBoxService;
        this.segmentationService = segmentationService;
    }

    private BoundingBoxDTO toBoundingBoxDto(ImageAnnotationBody imageAnnotationBody) {
        return BoundingBoxDTO.builder()
                .projectName(imageAnnotationBody.getProjectName())
                .imgUuid(imageAnnotationBody.getImgUuid())
                .imgW(imageAnnotationBody.getImgW())
                .imgH(imageAnnotationBody.getImgH())
                .imgX(imageAnnotationBody.getImgX())
                .imgY(imageAnnotationBody.getImgY())
                .imgDepth(imageAnnotationBody.getImgDepth())
                .imgOriginalHeight(imageAnnotationBody.getImgOriginalHeight())
                .imgOriginalWidth(imageAnnotationBody.getImgOriginalWidth())
                .imgBase64(imageAnnotationBody.getImgBase64())
                .boundingBoxPropertiesList(imageAnnotationBody.getBoundingBoxPropertiesList())
                .fileSize(imageAnnotationBody.getFileSize())
                .build();
    }

    private SegmentationDTO toSegmentationDto(ImageAnnotationBody imageAnnotationBody) {
        return SegmentationDTO.builder()
                .projectName(imageAnnotationBody.getProjectName())
                .imgUuid(imageAnnotationBody.getImgUuid())
                .imgW(imageAnnotationBody.getImgW())
                .imgH(imageAnnotationBody.getImgH())
                .imgX(imageAnnotationBody.getImgX())
                .imgY(imageAnnotationBody.getImgY())
                .imgDepth(imageAnnotationBody.getImgDepth())
                .imgOriginalHeight(imageAnnotationBody.getImgOriginalHeight())
                .imgOriginalWidth(imageAnnotationBody.getImgOriginalWidth())
                .imgBase64(imageAnnotationBody.getImgBase64())
                .segmentationPropertiesList(imageAnnotationBody.getSegmentationPropertiesList())
                .fileSize(imageAnnotationBody.getFileSize())
                .build();
    }

    @POST
    @Path("/imglabel/bndbox")
    public Future<ActionStatus> createBoundingBoxAnnotation(ImageAnnotationBody imageAnnotationBody) throws Exception {
        return boundingBoxService.createAnnotation(toBoundingBoxDto(imageAnnotationBody))
                .map(ActionStatus::okWithResponse)
                .otherwise(res -> ActionStatus.failedWithMessage("Fail to create bounding box"));
    }

    @POST
    @Path("/imglabel/seg")
    public Future<ActionStatus> createSegmentationAnnotation(ImageAnnotationBody imageAnnotationBody) throws Exception {
        return segmentationService.createAnnotation(toSegmentationDto(imageAnnotationBody))
                .map(ActionStatus::okWithResponse)
                .otherwise(res -> ActionStatus.failedWithMessage("Fail to create segmentation"));
    }

    @DELETE
    @Path("/v2/imglabel/bndbox/projects/{project_name}/uuids")
    public Future<ActionStatus> deleteBoundingBoxData(@PathParam("project_name") String projectName,
                                                      ImageAnnotationBody imageAnnotationBody) {
        return boundingBoxService.deleteData(projectName, imageAnnotationBody.getImgUuid())
                .map(ActionStatus.ok())
                .otherwise(res -> ActionStatus.failedWithMessage("Fail to delete data"));
    }

    @DELETE
    @Path("/v2/imglabel/seg/projects/{project_name}/uuids")
    public Future<ActionStatus> deleteSegmentationData(@PathParam("project_name") String projectName,
                                                      ImageAnnotationBody imageAnnotationBody) {
        return segmentationService.deleteData(projectName, imageAnnotationBody.getImgUuid())
                .map(ActionStatus.ok())
                .otherwise(res -> ActionStatus.failedWithMessage("Fail to delete data"));
    }

    @PUT
    @Path("/imglabel/bndbox/projects/{project_name}/uuid/{uuid}/update")
    public Future<ActionStatus> updateBoundingBoxData(@PathParam("project_name") String projectName,
                                                      ImageAnnotationBody imageAnnotationBody) throws Exception {
        return boundingBoxService.updateAnnotation(toBoundingBoxDto(imageAnnotationBody))
                .map(ActionStatus::okWithResponse)
                .otherwise(res -> ActionStatus.failedWithMessage("Fail to update data"));
    }

    @PUT
    @Path("/imglabel/seg/projects/{project_name}/uuid/{uuid}/update")
    public Future<ActionStatus> updateSegmentationData(@PathParam("project_name") String projectName,
                                                      ImageAnnotationBody imageAnnotationBody) throws Exception {
        return segmentationService.updateAnnotation(toSegmentationDto(imageAnnotationBody))
                .map(ActionStatus::okWithResponse)
                .otherwise(res -> ActionStatus.failedWithMessage("Fail to update data"));
    }

}
