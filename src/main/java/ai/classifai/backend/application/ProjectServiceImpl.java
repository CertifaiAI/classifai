package ai.classifai.backend.application;

import ai.classifai.backend.repository.database.DBUtils;
import ai.classifai.core.data.handler.ImageHandler;
import ai.classifai.core.dto.ProjectDTO;
import ai.classifai.core.entity.project.Project;
import ai.classifai.core.entity.project.ProjectEntity;
import ai.classifai.core.enumeration.AnnotationType;
import ai.classifai.core.enumeration.ProjectInfra;
import ai.classifai.core.loader.ProjectHandler;
import ai.classifai.core.loader.ProjectLoader;
import ai.classifai.core.loader.ProjectLoaderStatus;
import ai.classifai.core.service.project.ProjectRepository;
import ai.classifai.core.service.project.ProjectService;
import ai.classifai.core.status.FileSystemStatus;
import ai.classifai.core.utility.UuidGenerator;
import ai.classifai.core.versioning.Version;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class ProjectServiceImpl implements ProjectService {
    private final ProjectRepository projectRepoService;
    private final ProjectHandler projectHandler;

    public ProjectServiceImpl(ProjectRepository projectRepoService,
                              ProjectHandler projectHandler) {
        this.projectRepoService = projectRepoService;
        this.projectHandler = projectHandler;
    }

    private Project updateProjectParams(ProjectLoader loader, Project project) {
        File projectPath = new File(project.getProjectPath());
        List<String> existingDataInDir = ImageHandler.getValidImagesFromFolder(new File(project.getProjectPath()));
        Version currentVersion = loader.getProjectVersion().getCurrentVersion();

        project.setAnnotationType(loader.getAnnotationType());
        project.setTotalUuid(existingDataInDir.size());
        project.setIsLoaded(loader.getIsLoadedFrontEndToggle());
        project.setProjectInfra(loader.getProjectInfra().name());
        project.setCreatedDate(currentVersion.getCreatedDate().toString());
        project.setLastModifiedDate(currentVersion.getLastModifiedDate().toString());
        project.setCurrentVersion(currentVersion.getVersionUuid());
        project.setIsRootPathValid(projectPath.exists());

        return project;
    }

    @Override
    public Future<ProjectDTO> createProject(@NonNull ProjectDTO projectDTO) {
        projectDTO.setProjectId(UuidGenerator.generateUuid());

        ProjectLoader loader = ProjectLoader.builder()
                .projectId(projectDTO.getProjectId())
                .projectName(projectDTO.getProjectName())
                .annotationType(projectDTO.getAnnotationType())
                .projectPath(new File(projectDTO.getProjectPath()))
                .labelList(projectDTO.getLabelList())
                .projectLoaderStatus(ProjectLoaderStatus.LOADED)
                .projectInfra(ProjectInfra.ON_PREMISE)
                .fileSystemStatus(FileSystemStatus.ITERATING_FOLDER)
                .build();

        projectHandler.loadProjectLoader(loader);

        return projectRepoService.createProject(projectDTO)
                .map(res -> updateProjectParams(loader, res))
                .map(ProjectEntity::toDto);
    }

    @Override
    public Future<Optional<List<ProjectDTO>>> listProjects(@NonNull Integer projectType) {
        return projectRepoService.listProjects(projectType)
                .map(list -> list.stream()
                        .findFirst()
                        .map(res -> res.stream()
                                .map(ProjectEntity::toDto)
                                .collect(Collectors.toList())
                        )
                );
    }

    @Override
    public Future<Optional<ProjectDTO>> getProjectById(@NonNull ProjectLoader loader)
    {
        return projectRepoService.getProjectById(loader.getProjectId(), loader.getAnnotationType())
                .map(res -> updateProjectParams(loader, res.get()))
                .map(entity -> Optional.of(entity.toDto()));
    }


    @Override
    public Future<ProjectDTO> updateProject(@NonNull ProjectDTO projectDTO) {
        AnnotationType type = AnnotationType.get(projectDTO.getAnnotationType());
        ProjectLoader loader = projectHandler.getProjectLoader(projectDTO.getProjectName(), type);
        return getProjectById(loader)
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
                            .projectInfra(projectDTO.getProjectInfraParam().name())
                            .annotationType(projectDTO.getAnnotationType())
                            .build();

                    return projectRepoService.updateProject(projectEntity, projectDTO);
                })
                .map(ProjectEntity::toDto);
    }

    @Override
    public Future<Void> deleteProject(@NonNull ProjectDTO projectDTO) {
        AnnotationType type = AnnotationType.get(projectDTO.getAnnotationType());
        ProjectLoader loader = projectHandler.getProjectLoader(projectDTO.getProjectName(), type);
        return getProjectById(loader)
                .map(res -> res.stream()
                            .filter(entity -> entity.getProjectName().equals(projectDTO.getProjectName())
                                    && entity.getAnnotationType().equals(projectDTO.getAnnotationType()))
                            .findFirst())
                .compose(res -> {
                    Promise<ProjectDTO> promise = Promise.promise();
                    if (res.isPresent()) {
                        promise.complete(res.get());
                    } else {
                        promise.complete(null);
                    }
                    return promise.future();
                })
                .compose(res -> {
                    Project projectEntity = Project.builder()
                            .projectName(res.getProjectName())
                            .projectId(res.getProjectId())
                            .projectPath(res.getProjectPath())
                            .projectInfra(res.getProjectInfraParam().name())
                            .annotationType(res.getAnnotationType())
                            .build();

                    return projectRepoService.deleteProjectById(projectEntity);
                })
                .map(DBUtils::toVoid);
    }

    @Override
    public Future<Void> updateUuidVersionList(@NonNull ProjectLoader projectLoader) {
        return projectRepoService.updateUuidVersionList(projectLoader);
    }

    @Override
    public Future<Void> starProject(@NonNull String projectID, @NonNull Boolean isStarred) {
        return projectRepoService.startProject(projectID, isStarred);
    }

    @Override
    public Future<Void> updateLabels(@NonNull String projectID, @NonNull List<String> labelList) {
        return projectRepoService.updateLabels(projectID, labelList);
    }

    @Override
    public Future<Void> updateLastModifiedDate(@NonNull String projectID, @NonNull String dbFormat) {
        return projectRepoService.updateLastModifiedDate(projectID, dbFormat);
    }

    @Override
    public Future<Void> updateIsNewParam(@NonNull String projectID) {
        return projectRepoService.updateIsNewParam(projectID);
    }

}
