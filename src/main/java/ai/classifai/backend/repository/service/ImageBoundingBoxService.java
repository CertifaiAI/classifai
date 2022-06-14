package ai.classifai.backend.repository.service;

import ai.classifai.backend.data.handler.ImageHandler;
import ai.classifai.backend.repository.DBUtils;
import ai.classifai.core.dto.BoundingBoxDTO;
import ai.classifai.core.dto.properties.ImageProperties;
import ai.classifai.core.entity.annotation.ImageBoundingBoxEntity;
import ai.classifai.core.enumeration.ProjectType;
import ai.classifai.core.service.annotation.AnnotationRepository;
import ai.classifai.core.service.annotation.AnnotationService;
import ai.classifai.core.service.project.ProjectService;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Data
@Slf4j
public class ImageBoundingBoxService implements AnnotationService<BoundingBoxDTO, ImageProperties> {
    private final AnnotationRepository<ImageBoundingBoxEntity, BoundingBoxDTO, ImageProperties> annotationRepository;
    private final ProjectService projectService;

    public ImageBoundingBoxService(AnnotationRepository<ImageBoundingBoxEntity, BoundingBoxDTO, ImageProperties> annotationRepository,
                                   ProjectService projectService) {
        this.annotationRepository = annotationRepository;
        this.projectService = projectService;
    }

    @Override
    public Future<Void> parseData(ImageProperties imageProperties) {
        return annotationRepository.createAnnotationProject()
                .compose(res -> projectService.getProjectById(imageProperties.getProjectName(), ProjectType.IMAGEBOUNDINGBOX.ordinal())
                        .map(response -> response.get().getProjectId())
                )
                .compose(res -> Future.future(promise -> {
                    imageProperties.setProjectId(res);
                    promise.complete(imageProperties);
                }))
                .compose(res -> {
                    List<String> validImages = ImageHandler.getValidImagesFromFolder(new File(imageProperties.getProjectPath()));
                    List<Future> futures = new ArrayList<>();
                    validImages.forEach(path -> {
                        try {
                            futures.add(annotationRepository
                                    .saveFilesMetaData(ImageHandler.getImageProperties(imageProperties, new File(path))));
                        } catch (Exception e) {
                            log.info(e.getMessage());
                        }
                    });
                    return Future.future(promise -> {
                        CompositeFuture.all(futures)
                                .onFailure(promise::fail)
                                .onSuccess(promise::complete);
                    });
                })
                .map(DBUtils::toVoid);
    }

    @Override
    public Future<BoundingBoxDTO> createAnnotation(BoundingBoxDTO boundingBoxDTO) throws Exception {
        return annotationRepository.createAnnotation(boundingBoxDTO)
                .map(this::toDTO);
    }

    @Override
    public Future<List<BoundingBoxDTO>> listAnnotations(String projectName) {
        return annotationRepository.listAnnotation(projectName)
                .map(res -> res.stream().map(this::toDTO).collect(Collectors.toList()));
    }

    @Override
    public Future<Optional<BoundingBoxDTO>> getAnnotationById(String projectName, String uuid) {
        return listAnnotations(projectName)
                .map(res -> res.stream()
                        .filter(dto -> dto.getImgUuid().equals(uuid))
                        .findFirst()
                );
    }

    @Override
    public Future<Void> updateAnnotation(@NonNull BoundingBoxDTO boundingBoxDTO) throws Exception {
        return annotationRepository.updateAnnotation(boundingBoxDTO)
                .map(DBUtils::toVoid);
    }

    @Override
    public Future<Void> deleteData(@NonNull String projectName, @NonNull String uuid) {
        return annotationRepository.deleteData(projectName, uuid)
                .map(DBUtils::toVoid);
    }

    @Override
    public Future<Void> deleteProjectByName(String projectName) {
        return projectService.getProjectById(projectName, ProjectType.IMAGEBOUNDINGBOX.ordinal())
                        .map(response -> response.get().getProjectId())
                .compose(annotationRepository::deleteProjectByName)
                .map(DBUtils::toVoid);
    }

    private BoundingBoxDTO toDTO(ImageBoundingBoxEntity imageBoundingBoxEntity) {
        return BoundingBoxDTO.builder()
                .projectName(imageBoundingBoxEntity.getProjectName())
                .imgUuid(imageBoundingBoxEntity.getImgUuid())
                .imgOriginalWidth(imageBoundingBoxEntity.getImgOriginalWidth())
                .imgOriginalHeight(imageBoundingBoxEntity.getImgOriginalHeight())
                .imgDepth(imageBoundingBoxEntity.getImgDepth())
                .imgX(imageBoundingBoxEntity.getImgX())
                .imgY(imageBoundingBoxEntity.getImgY())
                .imgW(imageBoundingBoxEntity.getImgW())
                .imgH(imageBoundingBoxEntity.getImgH())
                .boundingBoxPropertiesList(imageBoundingBoxEntity.getBoundingBoxPropertiesList())
                .imgBase64(imageBoundingBoxEntity.getImgBase64())
                .fileSize(imageBoundingBoxEntity.getFileSize())
                .build();
    }
}
