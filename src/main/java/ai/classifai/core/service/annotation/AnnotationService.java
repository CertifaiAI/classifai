package ai.classifai.core.service.annotation;

import io.vertx.core.Future;
import lombok.NonNull;

import java.util.List;
import java.util.Optional;

public interface AnnotationService<T, U> {
    Future<Void> parseData(U properties) ;

    Future<T> createAnnotation(T annotationDTO) throws Exception;

    Future<List<T>> listAnnotations(String projectName);

    Future<Optional<T>> getAnnotationById(String projectName, String uuid);

    Future<Void> updateAnnotation(@NonNull T annotationDTO) throws Exception;

    Future<Void> deleteData(@NonNull String projectName, @NonNull String uuid);

    Future<Void> deleteProjectByName(@NonNull String projectName);

}
