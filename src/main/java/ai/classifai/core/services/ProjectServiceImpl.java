package ai.classifai.core.services;

import ai.classifai.dto.ProjectDTO;
import ai.classifai.repository.annotation.AnnotationType;
import ai.classifai.repository.project.ProjectRepoService;
import lombok.NonNull;

import javax.inject.Inject;
import java.util.List;

public class ProjectServiceImpl implements ProjectService {
    @Inject
    private ProjectRepoService projectRepoService;

    @Override
    public ProjectDTO createProject(@NonNull ProjectDTO projectDTO) {
        return projectRepoService.createProject(projectDTO);
    }

    @Override
    public List<ProjectDTO> listProjects() {
        return null;
    }

    @Override
    public ProjectDTO getProjectById(@NonNull String projectId) {
        return null;
    }

    @Override
    public ProjectDTO updateProject(@NonNull ProjectDTO projectDTO) {
        return null;
    }

    @Override
    public ProjectDTO deleteProject(@NonNull String projectId) {
        return null;
    }

    @Override
    public AnnotationType<?> createAnnotation(AnnotationType<?> annotation) {
        return null;
    }

    @Override
    public List<AnnotationType<?>> listAnnotation(String projectId) {
        return null;
    }

    @Override
    public AnnotationType<?> updateAnnotation(AnnotationType<?> annotation) {
        return null;
    }

    @Override
    public AnnotationType<?> deleteAnnotation(String annotationId) {
        return null;
    }

}
