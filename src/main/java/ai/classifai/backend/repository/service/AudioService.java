package ai.classifai.backend.repository.service;

import ai.classifai.core.dto.AudioDTO;
import ai.classifai.core.properties.AudioProperties;
import ai.classifai.core.entity.annotation.AudioEntity;
import ai.classifai.core.service.annotation.AnnotationRepository;
import ai.classifai.core.service.annotation.AnnotationService;
import ai.classifai.core.service.project.ProjectService;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import lombok.NonNull;

import java.util.List;
import java.util.Optional;

public class AudioService implements AnnotationService<AudioDTO, AudioProperties> {

    private final AnnotationRepository<AudioEntity, AudioDTO, AudioProperties> audioRepository;
    private final ProjectService projectService;

    public AudioService(AnnotationRepository<AudioEntity, AudioDTO, AudioProperties> audioRepository,
                        ProjectService projectService) {
        this.audioRepository = audioRepository;
        this.projectService = projectService;
    }

    private AudioProperties getAudioProperties(String audioPath) {
        return AudioProperties.builder().build();
    }

    @Override
    public Future<Void> parseData(AudioProperties properties) {
        Promise<Void> promise = Promise.promise();
//        return audioRepository.createAnnotationProject()
//                .map(res -> {
//                    try {
//                        return AudioHandler.generateWaveFormPeaks(properties.getProjectPath());
//                    } catch (IOException | UnsupportedAudioFileException e) {
//                        e.printStackTrace();
//                    }
//                    return null;
//                }).map(DBUtils::toVoid);
        return promise.future();
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
    public Future<Void> updateAnnotation(@NonNull AudioDTO annotationDTO) throws Exception {
        return null;
    }

    @Override
    public Future<Void> deleteData(@NonNull String projectName, @NonNull String uuid) {
        return null;
    }

    @Override
    public Future<Void> deleteProjectById(@NonNull String projectId) {
        return null;
    }

    private AudioDTO toDto(AudioEntity audioEntity) {
        return AudioDTO.builder().build();
    }
}
