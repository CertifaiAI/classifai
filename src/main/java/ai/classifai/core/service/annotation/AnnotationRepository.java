package ai.classifai.core.service.annotation;

import ai.classifai.core.loader.ProjectLoader;
import ai.classifai.core.loader.ProjectLoaderStatus;
import io.vertx.core.Future;
import lombok.NonNull;

import java.util.List;

public interface AnnotationRepository<T, U> {
    Future<T> createAnnotation(@NonNull U annotationDTO) throws Exception;

    Future<List<T>> listAnnotation(@NonNull String projectName);

    Future<Void> updateAnnotation(@NonNull U annotationDTO);

    Future<Void> saveFilesMetaData(U property);

    Future<Void> deleteData(@NonNull String projectName, @NonNull String uuid);

    Future<Void> createAnnotationProject();

    Future<Void> deleteProjectById(@NonNull String projectId);

    Future<ProjectLoaderStatus> loadAnnotationProject(@NonNull ProjectLoader projectLoader);

    Future<String> renameData(@NonNull ProjectLoader projectLoader, String uuid, String newFileName);

    void configProjectLoaderFromDb(@NonNull ProjectLoader loader);
}
