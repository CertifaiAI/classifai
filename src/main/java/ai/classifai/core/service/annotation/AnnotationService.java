package ai.classifai.core.service.annotation;

import lombok.NonNull;

import java.util.List;

public interface AnnotationService<T, U, K> {
    T toDTO(U type);

    List<T> listAnnotations();

    T createAnnotation(U annotation);

    T getAnnotationById(String id);

    T updateAnnotation(@NonNull U annotation);

    K setProperties(K properties);

    void deleteAnnotationById(String id);
}
