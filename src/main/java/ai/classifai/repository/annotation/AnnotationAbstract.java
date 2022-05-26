package ai.classifai.repository.annotation;

public abstract class AnnotationAbstract<T, U> {
    public abstract T toDTO(U type);
}
