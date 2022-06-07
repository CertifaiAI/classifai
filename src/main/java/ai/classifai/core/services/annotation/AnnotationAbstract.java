package ai.classifai.core.services.annotation;

import java.util.List;

public interface AnnotationAbstract<T, U> {
    T toDTO(U type);

    List<T> listAnnotations();

    T createAnnotation(U annotation);

    T getAnnotationById(String id);

    void deleteAnnotationById(String id);
}
