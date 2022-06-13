package ai.classifai.backend.repository.service;

import ai.classifai.backend.repository.DBUtils;
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

    public ProjectDTO toProjectDTOWithId(@NonNull ProjectEntity projectEntity) {
        return ProjectDTO.builder()
                .projectId(projectEntity.getProjectId())
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

    @Override
    public Future<Optional<ProjectDTO>> getProjectById(@NonNull String projectName, @NonNull Integer projectType) {
        return listProjectsByType(projectType)
                .map(res -> res.stream()
                        .filter(entity -> entity.getProjectName().equals(projectName) && entity.getProjectType().equals(projectType))
                        .findFirst())
                .compose(res -> res.map(projectEntity -> projectRepoService.getProjectById(projectEntity.getProjectId())
                        .map(Optional::get))
                        .orElse(null))
                .map(res -> Optional.ofNullable(toProjectDTOWithId(res)));
    }

    @Override
    public Future<Optional<ProjectDTO>> getProjectByNameAndType(@NonNull ProjectDTO projectDTO) {
        return projectRepoService.getProjectByNameAndType(projectRepoService.toProjectEntity(projectDTO))
                .map(res -> res.map(projectEntity -> Optional.ofNullable(toProjectDTO(projectEntity)))
                        .orElse(null));
    }

    @Override
    public Future<ProjectDTO> updateProject(@NonNull ProjectDTO projectDTO) {
        return listProjectsByType(projectDTO.getProjectType())
                .map(res ->  res.stream()
                            .filter(entity -> entity.getProjectName().equals(projectDTO.getProjectName())
                                    && entity.getProjectType().equals(projectDTO.getProjectType()))
                            .findFirst())
                .compose(res -> {
                    if (res.isEmpty()) {
                        throw new NullPointerException("Project " + projectDTO.getProjectName() + " not found");
                    }
                    return projectRepoService.updateProject(res.get(), projectDTO);
                })
                .map(this::toProjectDTO);
    }

    @Override
    public Future<Void> deleteProject(@NonNull ProjectDTO projectDTO) {
        return listProjectsByType(projectDTO.getProjectType())
                .map(res -> res.stream()
                            .filter(entity -> entity.getProjectName().equals(projectDTO.getProjectName())
                                    && entity.getProjectType().equals(projectDTO.getProjectType()))
                            .findFirst())
                .compose(res -> res.map(projectRepoService::deleteProjectById).orElse(null))
                .map(DBUtils::toVoid);
    }

    private Future<List<ProjectEntity>> listProjectsByType(Integer projectType) {
        return projectRepoService.listProjects(projectType);
    }
}
