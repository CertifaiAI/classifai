package ai.classifai.backend.repository.service;

import ai.classifai.backend.data.handler.ImageHandler;
import ai.classifai.backend.repository.DBUtils;
import ai.classifai.backend.utility.ParamConfig;
import ai.classifai.backend.utility.UuidGenerator;
import ai.classifai.core.dto.SegmentationDTO;
import ai.classifai.core.dto.properties.ImageProperties;
import ai.classifai.core.entity.annotation.ImageSegmentationEntity;
import ai.classifai.core.enumeration.ProjectType;
import ai.classifai.core.service.annotation.AnnotationRepository;
import ai.classifai.core.service.annotation.AnnotationService;
import ai.classifai.core.service.project.ProjectService;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import lombok.NonNull;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class ImageSegmentationService implements AnnotationService<SegmentationDTO, ImageProperties> {
    private final ProjectService projectService;
    private final AnnotationRepository<ImageSegmentationEntity, SegmentationDTO, ImageProperties> annotationRepository;

    public ImageSegmentationService(AnnotationRepository<ImageSegmentationEntity, SegmentationDTO, ImageProperties> annotationRepository,
                                    ProjectService projectService) {
        this.annotationRepository = annotationRepository;
        this.projectService = projectService;
    }

    private ImageProperties getImageProperties(ImageProperties imageProperties, File imageFile) throws Exception {
        Map<String, String> imageMetaData = ImageHandler.getImageMetaData(imageFile);

        imageProperties.setFileSize(FileUtils.sizeOf(imageFile));
        imageProperties.setImgUuid(UuidGenerator.generateUuid());
        imageProperties.setImgOriginalHeight(Integer.parseInt(imageMetaData.get(ParamConfig.getImgOriHParam())));
        imageProperties.setImgOriginalWidth(Integer.parseInt(imageMetaData.get(ParamConfig.getImgOriWParam())));
        imageProperties.setImgDepth(Integer.parseInt(imageMetaData.get(ParamConfig.getImgOriWParam())));
        imageProperties.setImgBase64(imageMetaData.get(ParamConfig.getImgThumbnailParam()));
        imageProperties.setImgPath(imageFile.getPath());

        return imageProperties;
    }

    @Override
    public Future<Void> parseData(ImageProperties imageProperties) {
        List<String> validImages = ImageHandler.getValidImagesFromFolder(new File(imageProperties.getProjectPath()));

        return annotationRepository.createAnnotationProject()
                .compose(res -> projectService.getProjectById(imageProperties.getProjectName(), ProjectType.IMAGEBOUNDINGBOX.ordinal())
                        .map(response -> response.get().getProjectId())
                )
                .compose(res -> {
                    Promise<ImageProperties> promise1 = Promise.promise();
                    imageProperties.setProjectId(res);
                    promise1.complete(imageProperties);
                    return promise1.future();
                })
                .compose(res -> {
                    Promise<Void> promise = Promise.promise();
                    for (String imgPath: validImages) {
                        try {
                            annotationRepository.saveFilesMetaData(getImageProperties(imageProperties, new File(imgPath)));
                        } catch (Exception e) {
                            promise.fail(e.getMessage());
                        }
                    }
                    promise.complete(null);
                    return promise.future();
                })
                .map(DBUtils::toVoid);
    }

    @Override
    public Future<SegmentationDTO> createAnnotation(SegmentationDTO segmentationDTO) throws Exception {
        return annotationRepository.createAnnotation(segmentationDTO)
                .map(this::toDTO);
    }

    @Override
    public Future<List<SegmentationDTO>> listAnnotations(String projectName) {
        return annotationRepository.listAnnotation(projectName)
                .map(res -> res.stream().map(this::toDTO).collect(Collectors.toList()));
    }

    @Override
    public Future<Optional<SegmentationDTO>> getAnnotationById(String projectName, String uuid) {
        return listAnnotations(projectName)
                .map(res -> res.stream()
                        .filter(dto -> dto.getImgUuid().equals(uuid))
                        .findFirst()
                );
    }

    @Override
    public Future<Void> updateAnnotation(@NonNull SegmentationDTO segmentationDTO) throws Exception {
        return annotationRepository.updateAnnotation(segmentationDTO)
                .map(DBUtils::toVoid);
    }

    @Override
    public Future<Void> deleteData(@NonNull String projectName, @NonNull String uuid) {
        return annotationRepository.deleteData(projectName, uuid)
                .map(DBUtils::toVoid);
    }

    @Override
    public Future<Void> deleteProjectByName(@NonNull String projectName) {
        return projectService.getProjectById(projectName, ProjectType.IMAGESEGMENTATION.ordinal())
                .map(response -> response.get().getProjectId())
                .compose(annotationRepository::deleteProjectByName)
                .map(DBUtils::toVoid);
    }

    private SegmentationDTO toDTO(ImageSegmentationEntity imageSegmentationEntity) {
        return SegmentationDTO.builder()
                .projectName(imageSegmentationEntity.getProjectName())
                .imgUuid(imageSegmentationEntity.getImgUuid())
                .imgOriginalWidth(imageSegmentationEntity.getImgOriginalWidth())
                .imgOriginalHeight(imageSegmentationEntity.getImgOriginalHeight())
                .imgDepth(imageSegmentationEntity.getImgDepth())
                .imgX(imageSegmentationEntity.getImgX())
                .imgY(imageSegmentationEntity.getImgY())
                .imgW(imageSegmentationEntity.getImgW())
                .imgH(imageSegmentationEntity.getImgH())
                .segmentationPropertiesList(imageSegmentationEntity.getSegmentationPropertiesList())
                .imgBase64(imageSegmentationEntity.getImgBase64())
                .fileSize(imageSegmentationEntity.getFileSize())
                .build();
    }
}
