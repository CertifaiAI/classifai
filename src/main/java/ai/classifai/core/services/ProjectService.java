package ai.classifai.core.services;

import ai.classifai.dto.ProjectDTO;
import ai.classifai.repository.annotation.AnnotationType;

import java.util.List;

public interface ProjectService {
    ProjectDTO createProject(ProjectDTO projectDTO);

    List<ProjectDTO> listProjects();

    ProjectDTO getProjectById(String projectId);

    ProjectDTO updateProject(ProjectDTO projectDTO);

    ProjectDTO deleteProject(String projectId);

    AnnotationType<?> createAnnotation(AnnotationType<?> annotation);

    List<AnnotationType<?>> listAnnotation(String projectId);

    AnnotationType<?> updateAnnotation(AnnotationType<?> annotation);

    AnnotationType<?> deleteAnnotation(String annotationId);


}
