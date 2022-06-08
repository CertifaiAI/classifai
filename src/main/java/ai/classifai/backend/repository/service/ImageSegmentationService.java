package ai.classifai.backend.repository.service;

import ai.classifai.core.dto.SegmentationDTO;
import ai.classifai.core.dto.properties.ImageProperties;
import ai.classifai.core.dto.properties.SegmentationProperties;
import ai.classifai.core.service.annotation.AnnotationService;
import ai.classifai.core.service.project.ProjectRepository;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;

public class ImageSegmentationService implements AnnotationService<SegmentationDTO, SegmentationProperties, ImageProperties> {
    private ImageProperties imageProperties;
    private List<SegmentationProperties> segmentationPropertiesList;
    private SegmentationDTO segmentationDTO;
    private final ProjectRepository projectRepoService;

    public ImageSegmentationService(ProjectRepository projectRepoService) {
        this.projectRepoService = projectRepoService;
    }

    @Override
    public ImageProperties setProperties(ImageProperties properties) {
        imageProperties = properties;
        return imageProperties;
    }

    @Override
    public SegmentationDTO createAnnotation(SegmentationProperties segmentationProperties) {
        this.segmentationPropertiesList.add(segmentationProperties);
        return toDTO(segmentationProperties);
    }

    @Override
    public SegmentationDTO getAnnotationById(String uuid) {
        SegmentationProperties segmentationPropertiesById = segmentationPropertiesList.stream()
                .filter(segmentationProperties -> segmentationProperties.getUuid().equals(uuid))
                .findFirst()
                .orElse(null);
        return toDTO(segmentationPropertiesById);
    }

    @Override
    public void deleteAnnotationById(String id) {
        this.segmentationPropertiesList.removeIf(segmentationProperties -> segmentationProperties.getUuid().equals(id));
    }

    @Override
    public SegmentationDTO updateAnnotation(@NonNull SegmentationProperties annotation) {
        return null;
    }

    @Override
    public List<SegmentationDTO> listAnnotations() {
        List<SegmentationDTO> segmentationDTOList = new ArrayList<>();
        for (SegmentationProperties properties : segmentationPropertiesList) {
            segmentationDTOList.add(toDTO(properties));
        }
        return segmentationDTOList;
    }

    @Override
    public SegmentationDTO toDTO(SegmentationProperties annotation) {
        this.segmentationDTO = SegmentationDTO.builder()
                .imgPath(imageProperties.getImgUuid())
                .imgPath(imageProperties.getImgPath())
                .imgOriginalWidth(imageProperties.getImgOriginalWidth())
                .imgOriginalHeight(imageProperties.getImgOriginalHeight())
                .imgX(imageProperties.getImgX())
                .imgY(imageProperties.getImgY())
                .imgW(imageProperties.getImgW())
                .imgH(imageProperties.getImgH())
                .segmentationPropertiesList(this.segmentationPropertiesList)
                .fileSize(imageProperties.getFileSize())
                .imgThumbnail(imageProperties.getImageThumbnail())
                .build();

        return segmentationDTO;
    }

}
