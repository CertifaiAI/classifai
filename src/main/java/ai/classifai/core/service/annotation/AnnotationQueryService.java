package ai.classifai.core.service.annotation;

import lombok.NonNull;

import java.util.List;

public interface AnnotationQueryService<T> {
    List<T> getAllAnnotation();

    T getAnnotationById(@NonNull String annotationId);

    void createAnnotation(@NonNull T annotationProperties);

    void updateAnnotation(@NonNull T annotationProperties);

    void deleteAnnotation(@NonNull String annotationId);
}
