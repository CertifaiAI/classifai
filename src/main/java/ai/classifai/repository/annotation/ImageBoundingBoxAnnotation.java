package ai.classifai.repository.annotation;

import ai.classifai.dto.BoundingBoxDTO;
import lombok.Data;
import lombok.Getter;

import java.util.Map;

@Data
public class ImageBoundingBoxAnnotation implements AnnotationType<BoundingBox>{
    private String imgUuid;
    private String imgPath;
    private Integer imgDepth;
    private Integer imgOriW;
    private Integer imgOriH;
    private Integer fileSize;
    private Map<String, BoundingBox> boundingBoxMap;
    @Getter
    private BoundingBoxDTO boundingBoxDTO;

    ImageBoundingBoxAnnotation(String imgUuid, String imgPath, Integer imgDepth,
                               Integer imgOriH, Integer imgOriW, Integer fileSize) {
        this.imgUuid = imgUuid;
        this.imgPath = imgPath;
        this.imgDepth = imgDepth;
        this.imgOriH = imgOriH;
        this.imgOriW = imgOriW;
        this.fileSize = fileSize;
    }

    @Override
    public BoundingBox createAnnotation(BoundingBox boundingBox) {
        return boundingBoxMap.put(boundingBox.getUuid(), boundingBox);
    }

    @Override
    public BoundingBox getAnnotationById(String annotationUuid) {
        return boundingBoxMap.get(annotationUuid);
    }

    @Override
    public void deleteAnnotationById(String id) {

    }

    @Override
    public void toDTO(BoundingBox annotation) {
        this.boundingBoxDTO = BoundingBoxDTO.builder()
                .uuid(annotation.getUuid())
                .label(annotation.getLabel())
                .color(annotation.getColor())
                .subLabel(annotation.getSubLabel())
                .img_x(annotation.getImg_x())
                .img_y(annotation.getImg_y())
                .img_w(annotation.getImg_w())
                .img_h(annotation.getImg_h())
                .lineWidth(annotation.getLineWidth())
                .build();
    }
}
