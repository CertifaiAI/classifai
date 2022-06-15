package ai.classifai.backend.repository.service;

import ai.classifai.backend.data.handler.ImageHandler;
import ai.classifai.backend.repository.database.DBUtils;
import ai.classifai.core.dto.SegmentationDTO;
import ai.classifai.core.properties.ImageProperties;
import ai.classifai.core.entity.annotation.ImageSegmentationEntity;
import ai.classifai.core.service.annotation.AnnotationRepository;
import ai.classifai.core.service.annotation.AnnotationService;
import ai.classifai.core.service.project.ProjectService;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class ImageSegmentationService implements AnnotationService<SegmentationDTO, ImageProperties> {
    private final ProjectService projectService;
    private final AnnotationRepository<ImageSegmentationEntity, SegmentationDTO, ImageProperties> annotationRepository;

    public ImageSegmentationService(AnnotationRepository<ImageSegmentationEntity, SegmentationDTO, ImageProperties> annotationRepository,
                                    ProjectService projectService) {
        this.annotationRepository = annotationRepository;
        this.projectService = projectService;
    }

    @Override
    public Future<Void> parseData(ImageProperties imageProperties) {
        return annotationRepository.createAnnotationProject()
                .compose(res -> projectService.getProjectById(imageProperties.getProjectId())
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
    public Future<Void> deleteProjectById(@NonNull String projectId) {
        return projectService.getProjectById(projectId)
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
