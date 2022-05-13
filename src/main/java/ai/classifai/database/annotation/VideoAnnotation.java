package ai.classifai.database.annotation;

import ai.classifai.database.annotation.properties.ImageProperties;

public class VideoAnnotation extends ImageAnnotation implements AnnotationProps<ImageProperties> {
    ImageProperties videoAnnotationProperties;

    VideoAnnotation(String projectId, Integer fileSize, Integer imgDepth,
                    Integer imgOriH, Integer imgOriW)
    {
        super(projectId, fileSize, imgDepth, imgOriH, imgOriW);
    }

    public ImageProperties getAnnotationProperties() {
        return videoAnnotationProperties;
    }
}
