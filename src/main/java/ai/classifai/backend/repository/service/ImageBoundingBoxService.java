package ai.classifai.backend.repository.service;

import ai.classifai.core.dto.BoundingBoxDTO;
import ai.classifai.core.dto.properties.BoundingBoxProperties;
import ai.classifai.core.dto.properties.ImageProperties;
import ai.classifai.core.service.annotation.AnnotationRepository;
import ai.classifai.core.service.annotation.AnnotationService;
import lombok.Data;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Data
@SuperBuilder
public class ImageBoundingBoxService implements AnnotationService<BoundingBoxDTO, BoundingBoxProperties, ImageProperties> {
    private final AnnotationRepository annotationRepository;
    private ImageProperties imageProperties;
    private List<BoundingBoxProperties> boundingBoxPropertiesList;
    private BoundingBoxDTO boundingBoxDTO;

    public ImageBoundingBoxService(AnnotationRepository annotationRepository) {
        this.annotationRepository = annotationRepository;
    }

    @Override
    public ImageProperties setProperties(ImageProperties imageProperties) {
        this.imageProperties = imageProperties;
        return imageProperties;
    }

    @Override
    public BoundingBoxDTO createAnnotation(BoundingBoxProperties boundingBoxProperties) {
        this.boundingBoxPropertiesList.add(boundingBoxProperties);
        return toDTO(boundingBoxProperties);
    }

    @Override
    public BoundingBoxDTO getAnnotationById(String uuid) {
        BoundingBoxProperties boundingBoxPropertiesById = this.boundingBoxPropertiesList.stream()
                .filter(boundingBoxProperties -> boundingBoxProperties.getUuid().equals(uuid))
                .findFirst()
                .orElse(null);
        return toDTO(boundingBoxPropertiesById);
    }

    @Override
    public void deleteAnnotationById(String id) {
        this.boundingBoxPropertiesList.removeIf(boundingBoxProperties -> boundingBoxProperties.getUuid().equals(id));
    }

    @Override
    public BoundingBoxDTO updateAnnotation(@NonNull BoundingBoxProperties annotation) {
        return null;
    }

    @Override
    public List<BoundingBoxDTO> listAnnotations() {
        List<BoundingBoxDTO> boundingBoxDTOList = new ArrayList<>();
        for (BoundingBoxProperties properties : boundingBoxPropertiesList) {
            boundingBoxDTOList.add(toDTO(properties));
        }
        return boundingBoxDTOList;
    }

    @Override
    public BoundingBoxDTO toDTO(BoundingBoxProperties boundingBoxProperties) {
        this.boundingBoxDTO = BoundingBoxDTO.builder()
                .imgUuid(imageProperties.getImgUuid())
                .imgPath(imageProperties.getImgPath())
                .imgOriginalWidth(imageProperties.getImgOriginalWidth())
                .imgOriginalHeight(imageProperties.getImgOriginalHeight())
                .imgX(imageProperties.getImgX())
                .imgY(imageProperties.getImgY())
                .imgW(imageProperties.getImgW())
                .imgH(imageProperties.getImgH())
                .boundingBoxPropertiesList(this.boundingBoxPropertiesList)
                .fileSize(imageProperties.getFileSize())
                .imgThumbnail(imageProperties.getImageThumbnail())
                .build();

        return boundingBoxDTO;
    }
}
