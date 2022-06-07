package ai.classifai.core.services.project;

import ai.classifai.backend.dto.ProjectDTO;
import ai.classifai.core.services.annotation.AnnotationAbstract;
import io.vertx.core.Future;
import lombok.NonNull;

import java.util.List;
import java.util.Optional;

public interface ProjectService {
    Future<ProjectDTO> createProject(@NonNull ProjectDTO projectDTO);

    Future<List<ProjectDTO>> listProjects(@NonNull Integer projectType);

    Future<Optional<ProjectDTO>> getProjectById(@NonNull String projectName, @NonNull Integer projectType);

    Future<ProjectDTO> updateProject(@NonNull String projectName, @NonNull Integer projectType);

    Future<Void> deleteProject(@NonNull String projectName, @NonNull Integer projectType);

    AnnotationAbstract createAnnotation(@NonNull AnnotationAbstract annotation);

    List<AnnotationAbstract> listAnnotation(@NonNull String projectId);

    AnnotationAbstract updateAnnotation(@NonNull AnnotationAbstract annotation);

    AnnotationAbstract deleteAnnotation(@NonNull String annotationId);

}
