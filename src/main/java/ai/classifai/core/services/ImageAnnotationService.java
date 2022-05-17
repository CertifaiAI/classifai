package ai.classifai.core.services;

import ai.classifai.core.application.AnnotationService;
import ai.classifai.database.annotation.ImageAnnotation;
import lombok.NonNull;

import java.util.List;

public class ImageAnnotationService implements AnnotationService<ImageAnnotation> {
    @Override
    public List<ImageAnnotation> getAllAnnotation() {
        return null;
    }

    @Override
    public ImageAnnotation filterAnnotationById(@NonNull String annotationId) {
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
