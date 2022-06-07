package ai.classifai.backend.repository.entity.annotation;

import ai.classifai.core.services.annotation.AnnotationAbstract;
import ai.classifai.backend.dto.BoundingBoxDTO;
import ai.classifai.backend.dto.properties.BoundingBoxProperties;
import ai.classifai.backend.dto.properties.ImageProperties;
import lombok.Data;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Data
public class ImageBoundingBoxAnnotation implements AnnotationAbstract<BoundingBoxDTO, BoundingBoxProperties> {
    private ImageProperties imageProperties;
    private List<BoundingBoxProperties> boundingBoxPropertiesList;
    @Getter
    private BoundingBoxDTO boundingBoxDTO;

    public ImageBoundingBoxAnnotation(ImageProperties imageProperties) {
        this.imageProperties = imageProperties;
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
