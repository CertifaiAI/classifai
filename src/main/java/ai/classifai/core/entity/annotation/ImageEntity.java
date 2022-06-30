package ai.classifai.core.entity.annotation;

import ai.classifai.core.properties.image.DataInfoProperties;
import ai.classifai.core.properties.image.ImageDTO;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class ImageEntity implements AnnotationEntity<ImageDTO> {
    String uuid;

    String projectId;

    String imgPath;

    Map<String, DataInfoProperties> annotationDict;

    Integer imgDepth;

    Integer imgOriW;

    Integer imgOriH;

    Long fileSize;

    @Override
    public ImageDTO toDto() {
        return ImageDTO.builder()
                .uuid(uuid)
                .projectId(projectId)
                .imgPath(imgPath)
                .annotationDict(annotationDict)
                .imgDepth(imgDepth)
                .imgOriW(imgOriW)
                .imgOriH(imgOriH)
                .fileSize(fileSize)
                .build();
    }
}
