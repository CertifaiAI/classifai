package ai.classifai.core.application;

import ai.classifai.core.services.annotation.AnnotationQueryService;
import ai.classifai.database.annotation.ImageAnnotation;
import lombok.NonNull;

import java.util.List;

public class ImageAnnotationQueryService implements AnnotationQueryService<ImageAnnotation> {
    @Override
    public List<ImageAnnotation> getAllAnnotation() {
        return null;
    }

    @Override
    public ImageAnnotation getAnnotationById(@NonNull String annotationId) {
        return null;
    }

    @Override
    public void createAnnotation(@NonNull ImageAnnotation annotationProperties) {

    }

    @Override
    public void updateAnnotation(@NonNull ImageAnnotation annotationProperties) {

    }

    @Override
    public void deleteAnnotation(@NonNull String annotationId) {

    }
}
