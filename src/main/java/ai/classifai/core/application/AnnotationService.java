package ai.classifai.core.application;

import lombok.NonNull;

import java.util.List;

public interface AnnotationService<T> {
    List<T> getAllAnnotation();

    T filterAnnotationById(@NonNull String annotationId);

    void createAnnotation(@NonNull T annotationProperties);

    void updateAnnotation(@NonNull T annotationProperties);

    void deleteAnnotation(@NonNull String annotationId);
}
