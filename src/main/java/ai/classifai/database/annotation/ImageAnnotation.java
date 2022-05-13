package ai.classifai.database.annotation;

import ai.classifai.database.annotation.properties.ImageProperties;

public class ImageAnnotation extends Annotation implements AnnotationProps<ImageProperties> {
    private ImageProperties imageProperties;
    private Integer imgDepth;
    private Integer imgOriW;
    private Integer imgOriH;

    ImageAnnotation(String projectId, Integer fileSize, Integer imgDepth, Integer imgOriH, Integer imgOriW) {
        super(projectId, fileSize);
        this.imgDepth = imgDepth;
        this.imgOriH = imgOriH;
        this.imgOriW = imgOriW;
    }

    public ImageProperties getAnnotationProperties() {
        return imageProperties;
    }

}
