package ai.classifai.core.service.annotation;

import ai.classifai.core.dto.ProjectDTO;
import ai.classifai.core.loader.ProjectLoader;
import ai.classifai.core.loader.ProjectLoaderStatus;
import io.vertx.core.Future;
import lombok.NonNull;

import java.util.List;
import java.util.Optional;

public interface AnnotationService<T, U> {
    Future<ProjectLoader> createAnnotationProject(ProjectDTO projectDTO) throws Exception;

    Future<T> createAnnotation(T annotationDTO) throws Exception;

    Future<List<T>> listAnnotations(String projectName);

    Future<Optional<T>> getAnnotationById(String projectName, String uuid);

    Future<Void> updateAnnotation(@NonNull T annotationDTO, @NonNull ProjectLoader projectLoader);

    Future<Void> deleteData(@NonNull String projectName, @NonNull String uuid);

    Future<Void> deleteProjectById(@NonNull ProjectDTO projectDTO);

    Future<ProjectLoaderStatus> loadProject(ProjectLoader projectLoader);

    Future<String> renameData(@NonNull ProjectLoader projectLoader, String uuid, String newFileName);

    T toDTO(U property, @NonNull ProjectLoader loader);
}
