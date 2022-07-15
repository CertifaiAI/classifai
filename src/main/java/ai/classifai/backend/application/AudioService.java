package ai.classifai.backend.application;

import ai.classifai.core.data.handler.AudioHandler;
import ai.classifai.core.dto.AudioDTO;
import ai.classifai.core.dto.ProjectDTO;
import ai.classifai.core.entity.annotation.AudioEntity;
import ai.classifai.core.loader.ProjectHandler;
import ai.classifai.core.loader.ProjectLoader;
import ai.classifai.core.loader.ProjectLoaderStatus;
import ai.classifai.core.properties.audio.AudioProperties;
import ai.classifai.core.properties.audio.AudioRegionsProperties;
import ai.classifai.core.service.annotation.AudioAnnotationService;
import ai.classifai.core.service.annotation.AudioDataRepository;
import ai.classifai.core.service.project.ProjectService;
import ai.classifai.core.status.FileSystemStatus;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public class AudioService implements AudioAnnotationService<AudioDTO, AudioProperties> {

    private final AudioDataRepository<AudioEntity, AudioDTO> audioDataRepository;
    private final ProjectService projectService;
    private final ProjectHandler projectHandler;

    public AudioService(AudioDataRepository<AudioEntity, AudioDTO> audioDataRepository,
                        ProjectService projectService,
                        ProjectHandler projectHandler)
    {
        this.audioDataRepository = audioDataRepository;
        this.projectService = projectService;
        this.projectHandler = projectHandler;
    }

    @Override
    public Future<ProjectLoader> createAnnotationProject(ProjectDTO projectDTO) throws UnsupportedAudioFileException, IOException {
        Promise<ProjectLoader> promise = Promise.promise();
        ProjectLoader loader = projectHandler.getProjectLoader(projectDTO.getProjectId());
        AudioHandler audioHandler = new AudioHandler();
        List<Integer> peaks = audioHandler.generateWaveFormPeaks(loader.getProjectFilePath().getPath());
        List<Double> timeStampList = audioHandler.getTimeStamp();
        AudioDTO audioDTO = audioHandler.toDTO(loader);
        List<Future> futures = new ArrayList<>();

        audioDataRepository.createAnnotationProject()
                .onComplete(res -> {
                    if (res.succeeded()) {
                        futures.add(audioDataRepository.saveFilesMetaData(audioDTO));
                        for (int i = 0; i < peaks.size(); i++)
                        {
                            futures.add(audioDataRepository.saveWaveFormPeaks(loader.getProjectId(), timeStampList.get(i),
                                    peaks.get(i), projectDTO.getProjectFilePath()));
                        }

                        Future.future(promises ->
                        {
                            CompositeFuture.all(futures)
                                    .onFailure(promises::fail)
                                    .onSuccess(promises::complete);
                        });
                        loader.setFileSystemStatus(FileSystemStatus.DATABASE_UPDATED);
                        promise.complete(loader);
                    }

                    else if (res.failed()) {
                        promise.fail(res.cause());
                    }
                });

        return promise.future();
    }

    @Override
    public Future<AudioDTO> createAnnotation(AudioDTO audioDTO) throws Exception {
        return audioDataRepository.createAnnotation(audioDTO)
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
        Promise<ProjectLoaderStatus> promise = Promise.promise();

        audioDataRepository.loadAnnotationProject(projectLoader)
                .onComplete(res -> {
                    if (res.succeeded()) {
                        if (projectLoader.getIsProjectNew()) {
                            projectService.updateIsNewParam(projectLoader.getProjectId())
                                    .onFailure(promise::fail);
                        }

                        promise.complete(res.result());
                    }

                    if (res.failed()) {
                        promise.fail(res.cause());
                    }
                });

        return promise.future();
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

    @Override
    public Future<List<Integer>> getWaveFormPeaks(@NonNull ProjectLoader projectLoader) {
        return audioDataRepository.getWaveFormPeaks(projectLoader);
    }

    @Override
    public Future<List<AudioRegionsProperties>> getAudioRegions(@NonNull String projectId) {
        return audioDataRepository.getAudioRegions(projectId);
    }

    @Override
    public Future<Void> exportAudioAnnotation(@NonNull ProjectLoader projectLoader) {
        return null;
    }
}
