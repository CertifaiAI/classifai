package ai.classifai.backend.application;

import ai.classifai.core.dto.ProjectDTO;
import ai.classifai.core.dto.VideoDTO;
import ai.classifai.core.entity.annotation.VideoEntity;
import ai.classifai.core.loader.ProjectLoader;
import ai.classifai.core.loader.ProjectLoaderStatus;
import ai.classifai.core.properties.video.VideoProperties;
import ai.classifai.core.service.annotation.AnnotationRepository;
import ai.classifai.core.service.annotation.AnnotationService;
import ai.classifai.core.service.annotation.VideoAnnotationService;
import ai.classifai.core.service.project.ProjectService;
import io.vertx.core.Future;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

@Slf4j
public class VideoService implements VideoAnnotationService<VideoDTO, VideoProperties> {

    private final AnnotationRepository<VideoEntity, VideoDTO> videoRepoService;
    private final ProjectService projectService;

    public VideoService(AnnotationRepository<VideoEntity, VideoDTO> videoRepoService,
                        ProjectService projectService) {
        this.videoRepoService = videoRepoService;
        this.projectService = projectService;
    }

    @Override
    public Future<ProjectLoader> createAnnotationProject(ProjectDTO projectDTO) throws Exception {
        return videoRepoService.createAnnotationProject()
                .mapEmpty();
    }

    @Override
    public Future<VideoDTO> createAnnotation(VideoDTO annotationDTO) throws Exception {
        return null;
    }

    @Override
    public Future<List<VideoDTO>> listAnnotations(String projectName) {
        return null;
    }

    @Override
    public Future<Optional<VideoDTO>> getAnnotationById(String projectName, String uuid) {
        return null;
    }

    @Override
    public Future<Void> updateAnnotation(@NonNull VideoDTO annotationDTO, @NonNull ProjectLoader projectLoader) {
        return null;
    }

    @Override
    public Future<Void> deleteData(@NonNull String projectName, @NonNull String uuid) {
        return null;
    }

    @Override
    public Future<Void> deleteProjectById(@NonNull ProjectDTO projectDTO) {
        return null;
    }

    @Override
    public Future<ProjectLoaderStatus> loadProject(ProjectLoader projectLoader) {
        return null;
    }

    @Override
    public Future<String> renameData(@NonNull ProjectLoader projectLoader, String uuid, String newFileName) {
        return null;
    }

    @Override
    public VideoDTO toDTO(VideoProperties property, @NonNull ProjectLoader loader) {
        return null;
    }
}
