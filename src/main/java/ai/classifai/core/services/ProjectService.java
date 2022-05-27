package ai.classifai.core.services;

import ai.classifai.dto.ProjectDTO;
import ai.classifai.repository.annotation.AnnotationType;
import ai.classifai.repository.project.ProjectEntity;
import io.vertx.core.Future;

import java.util.List;

public interface ProjectService {
    Future<ProjectDTO> createProject(ProjectDTO projectDTO);

    List<ProjectDTO> listProjects();

    ProjectDTO getProjectById(ProjectDTO projectDTO);

    ProjectDTO updateProject(ProjectDTO projectDTO);

    ProjectDTO deleteProject(ProjectDTO projectDTO);

    ProjectEntity toProjectEntity(ProjectDTO projectDTO);

    AnnotationType<?> createAnnotation(AnnotationType<?> annotation);

    List<AnnotationType<?>> listAnnotation(String projectId);

    AnnotationType<?> updateAnnotation(AnnotationType<?> annotation);

    AnnotationType<?> deleteAnnotation(String annotationId);


}
