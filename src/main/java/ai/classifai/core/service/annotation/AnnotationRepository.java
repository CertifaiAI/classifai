package ai.classifai.core.service.annotation;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.vertx.core.Future;
import lombok.NonNull;

import java.util.List;

public interface AnnotationRepository<T, U, K> {
    Future<T> createAnnotation(@NonNull U annotationDTO) throws JsonProcessingException;

    Future<List<T>> listAnnotation(@NonNull String projectName);

    Future<Void> updateAnnotation(@NonNull U annotationDTO) throws Exception;

    Future<Void> saveFilesMetaData(K property);

    Future<Void> deleteData(@NonNull String projectName, @NonNull String uuid);

    Future<Void> createAnnotationProject();

    Future<Void> deleteProjectByName(@NonNull String projectName);
}
