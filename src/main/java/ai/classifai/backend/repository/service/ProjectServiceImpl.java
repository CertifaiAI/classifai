package ai.classifai.backend.repository.service;

import ai.classifai.backend.repository.database.DBUtils;
import ai.classifai.backend.utility.UuidGenerator;
import ai.classifai.core.dto.ProjectDTO;
import ai.classifai.core.entity.project.Project;
import ai.classifai.core.entity.project.ProjectEntity;
import ai.classifai.core.service.project.ProjectRepository;
import ai.classifai.core.service.project.ProjectService;
import io.vertx.core.Future;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class ProjectServiceImpl implements ProjectService {
    private final ProjectRepository projectRepoService;

    public ProjectServiceImpl(ProjectRepository projectRepoService) {
        this.projectRepoService = projectRepoService;
    }

    @Override
    public Future<ProjectDTO> createProject(@NonNull ProjectDTO projectDTO) {
        projectDTO.setProjectId(UuidGenerator.generateUuid());
        return projectRepoService.createProject(projectDTO)
                .map(ProjectEntity::toDto);
    }

    @Override
    public Future<List<ProjectDTO>> listProjects(@NonNull Integer projectType) {
        return projectRepoService.listProjects(projectType)
                .map(entityList -> entityList.stream()
                        .map(ProjectEntity::toDto)
                        .collect(Collectors.toList()));
    }

    @Override
    public Future<Optional<ProjectDTO>> getProjectById(@NonNull String projectId) {
        return projectRepoService.getProjectById(projectId)
                        .map(res -> res.map(ProjectEntity::toDto));
    }

    @Override
    public Future<ProjectDTO> updateProject(@NonNull ProjectDTO projectDTO) {
        return listProjects(projectDTO.getAnnotationType())
                .map(res ->  res.stream()
                            .filter(entity -> entity.getProjectName().equals(projectDTO.getProjectName())
                                    && entity.getAnnotationType().equals(projectDTO.getAnnotationType()))
                            .findFirst())
                .compose(res -> {
                    if (res.isEmpty()) {
                        throw new NullPointerException("Project " + projectDTO.getProjectName() + " not found");
                    }
                    Project projectEntity = Project.builder()
                            .projectName(projectDTO.getProjectName())
                            .projectId(projectDTO.getProjectId())
                            .projectPath(projectDTO.getProjectPath())
                            .projectInfra(projectDTO.getProjectInfra())
                            .annotationType(projectDTO.getAnnotationType())
                            .labelList(projectDTO.getLabelList())
                            .build();

                    return projectRepoService.updateProject(projectEntity, projectDTO);
                })
                .map(ProjectEntity::toDto);
    }

    @Override
    public Future<Void> deleteProject(@NonNull ProjectDTO projectDTO) {
        return listProjects(projectDTO.getAnnotationType())
                .map(res -> res.stream()
                            .filter(entity -> entity.getProjectName().equals(projectDTO.getProjectName())
                                    && entity.getAnnotationType().equals(projectDTO.getAnnotationType()))
                            .findFirst())
                .compose(res -> {
                    Project projectEntity = Project.builder()
                            .projectName(projectDTO.getProjectName())
                            .projectId(projectDTO.getProjectId())
                            .projectPath(projectDTO.getProjectPath())
                            .projectInfra(projectDTO.getProjectInfra())
                            .annotationType(projectDTO.getAnnotationType())
                            .labelList(projectDTO.getLabelList())
                            .build();
                    return projectRepoService.deleteProjectById(projectEntity);
                })
                .map(DBUtils::toVoid);
    }

}
