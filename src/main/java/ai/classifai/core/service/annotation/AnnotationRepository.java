package ai.classifai.core.service.annotation;

import io.vertx.core.Future;
import lombok.NonNull;

import java.util.List;
import java.util.Optional;

public interface AnnotationRepository<T, U> {
    Future<T> createAnnotation(@NonNull U annotationDTO);

    Future<List<T>> listAnnotation();

    Future<T> updateAnnotation(@NonNull U annotationDTO);

    Future<Optional<T>> getAnnotationById(@NonNull String id);

    Future<Void> deleteProjectById(@NonNull U annotationDTO);

    T toAnnotationEntity(@NonNull U annotationDTO);
}
