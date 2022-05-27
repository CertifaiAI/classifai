package ai.classifai.core.services;

import ai.classifai.dto.ProjectDTO;
import ai.classifai.repository.annotation.AnnotationType;
import ai.classifai.repository.project.ProjectEntity;
import ai.classifai.repository.service.ProjectRepoService;
import io.vertx.core.Future;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.util.List;

@Slf4j
public class ProjectServiceImpl implements ProjectService {
    @Inject
    private ProjectRepoService projectRepoService;

    @Override
    public ProjectEntity toProjectEntity(@NonNull ProjectDTO projectDTO) {
        return ProjectEntity.builder()
                .projectId(projectDTO.getProjectId())
                .projectName(projectDTO.getProjectName())
                .projectType(projectDTO.getProjectType())
                .projectPath(projectDTO.getProjectPath())
                .projectInfra(projectDTO.getProjectInfra())
                .labelList(projectDTO.getLabelList())
                .build();
    }

    @Override
    public Future<ProjectDTO> createProject(@NonNull ProjectDTO projectDTO) {
        return projectRepoService.createProject(toProjectEntity(projectDTO));
    }

    @Override
    public List<ProjectDTO> listProjects() {
        return projectRepoService.listProjects();
    }

    @Override
    public ProjectDTO getProjectById(@NonNull ProjectDTO projectDTO) {
        return projectRepoService.getProjectById(toProjectEntity(projectDTO));
    }

    @Override
    public ProjectDTO updateProject(@NonNull ProjectDTO projectDTO) {
        return projectRepoService.updateProject(toProjectEntity(projectDTO));
    }

    @Override
    public ProjectDTO deleteProject(@NonNull ProjectDTO projectDTO) {
        return projectRepoService.deleteProjectById(toProjectEntity(projectDTO));
    }

    @Override
    public AnnotationType<?> createAnnotation(@NonNull AnnotationType<?> annotation) {
        return null;
    }

    @Override
    public List<AnnotationType<?>> listAnnotation(@NonNull String projectId) {
        return null;
    }

    @Override
    public AnnotationType<?> updateAnnotation(@NonNull AnnotationType<?> annotation) {
        return null;
    }

    @Override
    public AnnotationType<?> deleteAnnotation(@NonNull String annotationId) {
        return null;
    }

}
