package ai.classifai.backend.repository.service;

import ai.classifai.backend.utility.UuidGenerator;
import ai.classifai.core.dto.ProjectDTO;
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

    public ProjectDTO toProjectDTO(@NonNull ProjectEntity projectEntity) {
        return ProjectDTO.builder()
                .projectName(projectEntity.getProjectName())
                .projectType(projectEntity.getProjectType())
                .labelList(projectEntity.getLabelList())
                .build();
    }

    @Override
    public Future<ProjectDTO> createProject(@NonNull ProjectDTO projectDTO) {
        projectDTO.setProjectId(UuidGenerator.generateUuid());
        return projectRepoService.createProject(projectDTO)
                .map(this::toProjectDTO);
    }

    @Override
    public Future<List<ProjectDTO>> listProjects(Integer projectType) {
        return projectRepoService.listProjects(projectType)
                .map(entityList -> entityList.stream()
                        .map(this::toProjectDTO)
                        .collect(Collectors.toList()));
    }

    private Future<List<ProjectDTO>> listProjectsByType(Integer projectType) {
        return projectRepoService.listProjects(projectType)
                .map(entityList -> entityList.stream()
                        .map(res -> ProjectDTO.builder()
                                .projectId(res.getProjectId())
                                .projectName(res.getProjectName())
                                .projectType(res.getProjectType())
                                .labelList(res.getLabelList())
                                .build())
                        .collect(Collectors.toList()));
    }

    // getProject name
    @Override
    public Future<Optional<ProjectDTO>> getProjectById(@NonNull String projectName, @NonNull Integer projectType) {
        return listProjectsByType(projectType)
                .map(res -> res.stream()
                        .filter(entity -> entity.getProjectName().equals(projectName) && entity.getProjectType().equals(projectType))
                        .findFirst())
                .compose(res -> projectRepoService.getProjectById(res.get().getProjectId()))
                .map(res -> Optional.ofNullable(toProjectDTO(res.get())));
    }

    @Override
    public Future<ProjectDTO> updateProject(@NonNull String projectName, @NonNull Integer projectType) {
        return listProjectsByType(projectType)
                .map(res ->  res.stream()
                            .filter(entity -> entity.getProjectName().equals(projectName) && entity.getProjectType().equals(projectType))
                            .findFirst())
                .compose(res -> {
                    if (res.isEmpty()) {
                        throw new NullPointerException("Project not found");
                    }
                    return projectRepoService.updateProject(projectRepoService.toProjectEntity(res.get()), res.get());
                })
                .map(this::toProjectDTO);
    }

    @Override
    public Future<Void> deleteProject(@NonNull String projectName, @NonNull Integer projectType) {
        return listProjectsByType(projectType)
                .map(res -> res.stream()
                            .filter(entity -> entity.getProjectName().equals(projectName) && entity.getProjectType().equals(projectType))
                            .findFirst())
                .compose(res -> {
                    if (res.isEmpty()) {
                        throw new NullPointerException("Project not found");
                    }
                    return projectRepoService.getProjectById(res.get().getProjectId()).map(Optional::get);
                })
                .compose(projectRepoService::deleteProjectById)
                .map(res -> null);
    }

}
