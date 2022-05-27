package ai.classifai.repository.annotation;

import ai.classifai.dto.BoundingBoxDTO;
import ai.classifai.dto.properties.BoundingBox;
import ai.classifai.dto.properties.ImageProperties;
import lombok.Data;
import lombok.Getter;

import java.util.List;

@Data
public class ImageBoundingBoxAnnotation extends AnnotationAbstract<BoundingBoxDTO, BoundingBox> implements AnnotationType<BoundingBox>{
    private ImageProperties imageProperties;
    private List<BoundingBox> boundingBoxList;
    @Getter
    private BoundingBoxDTO boundingBoxDTO;

    ImageBoundingBoxAnnotation(ImageProperties imageProperties) {
        super();
        this.imageProperties = imageProperties;
    }

    @Override
    public BoundingBox createAnnotation(BoundingBox boundingBox) {
        this.boundingBoxList.add(boundingBox);
        return boundingBox;
    }

    @Override
    public BoundingBox getAnnotationById(String uuid) {
        BoundingBox boundingBoxById = this.boundingBoxList.stream()
                .filter(boundingBox -> boundingBox.getUuid().equals(uuid))
                .findFirst()
                .orElse(null);
        return boundingBoxById;
    }

    @Override
    public void deleteAnnotationById(String id) {
        this.boundingBoxList.removeIf(boundingBox -> boundingBox.getUuid().equals(id));
    }

    @Override
    public BoundingBoxDTO toDTO(BoundingBox boundingBox) {
        this.boundingBoxDTO = BoundingBoxDTO.builder()
                .imgUuid(imageProperties.getImgUuid())
                .imgPath(imageProperties.getImgPath())
                .imgOriginalWidth(imageProperties.getImgOriginalWidth())
                .imgOriginalHeight(imageProperties.getImgOriginalHeight())
                .imgX(imageProperties.getImgX())
                .imgY(imageProperties.getImgY())
                .imgW(imageProperties.getImgW())
                .imgH(imageProperties.getImgH())
                .boundingBoxList(this.boundingBoxList)
                .fileSize(imageProperties.getFileSize())
                .imgThumbnail(imageProperties.getImageThumbnail())
                .build();

        return boundingBoxDTO;
    }
}
