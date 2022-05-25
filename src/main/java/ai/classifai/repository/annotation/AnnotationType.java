package ai.classifai.repository.annotation;

public interface AnnotationType<T> {
    T createAnnotation(T annotation);

    T getAnnotationById(String id);

    void deleteAnnotationById(String id);

    void toDTO(T annotation);
}
