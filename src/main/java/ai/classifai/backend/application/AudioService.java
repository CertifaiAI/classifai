package ai.classifai.backend.application;

import ai.classifai.core.dto.AudioDTO;
import ai.classifai.core.dto.ProjectDTO;
import ai.classifai.core.entity.annotation.AudioEntity;
import ai.classifai.core.loader.ProjectLoader;
import ai.classifai.core.loader.ProjectLoaderStatus;
import ai.classifai.core.properties.audio.AudioProperties;
import ai.classifai.core.service.annotation.AnnotationRepository;
import ai.classifai.core.service.annotation.AnnotationService;
import ai.classifai.core.service.project.ProjectService;
import io.vertx.core.Future;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

@Slf4j
public class AudioService implements AnnotationService<AudioDTO, AudioProperties> {

    private final AnnotationRepository<AudioEntity, AudioDTO> audioRepository;
    private final ProjectService projectService;

    public AudioService(AnnotationRepository<AudioEntity, AudioDTO> audioRepository,
                        ProjectService projectService) {
        this.audioRepository = audioRepository;
        this.projectService = projectService;
    }

    @Override
    public Future<ProjectLoader> createAnnotationProject(ProjectDTO projectDTO) {
        return null;
    }

    @Override
    public Future<AudioDTO> createAnnotation(AudioDTO audioDTO) throws Exception {
        return audioRepository.createAnnotation(audioDTO)
                .map(this::toDto);
    }

    @Override
    public Future<List<AudioDTO>> listAnnotations(String projectName) {
        return null;
    }

    @Override
    public Future<Optional<AudioDTO>> getAnnotationById(String projectName, String uuid) {
        return null;
    }

    @Override
    public Future<Void> updateAnnotation(@NonNull AudioDTO annotationDTO, @NonNull ProjectLoader projectLoader) {
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
    public AudioDTO toDTO(AudioProperties property, @NonNull ProjectLoader loader) {
        return null;
    }

    private AudioDTO toDto(AudioEntity audioEntity) {
        return AudioDTO.builder().build();
    }
}
