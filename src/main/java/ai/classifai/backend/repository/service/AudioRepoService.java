package ai.classifai.backend.repository.service;

import ai.classifai.backend.repository.JDBCPoolHolder;
import ai.classifai.backend.repository.database.DBUtils;
import ai.classifai.backend.repository.query.AnnotationQuery;
import ai.classifai.backend.repository.query.QueryOps;
import ai.classifai.core.dto.AudioDTO;
import ai.classifai.core.loader.ProjectLoader;
import ai.classifai.core.loader.ProjectLoaderStatus;
import ai.classifai.core.properties.audio.AudioProperties;
import ai.classifai.core.entity.annotation.AudioEntity;
import ai.classifai.core.service.annotation.AnnotationRepository;
import ai.classifai.frontend.request.ThumbnailProperties;
import io.vertx.core.Future;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.Tuple;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class AudioRepoService implements AnnotationRepository<AudioEntity, AudioDTO> {
    private final JDBCPool annotationPool;
    private final QueryOps queryOps = new QueryOps();

    public AudioRepoService(JDBCPoolHolder jdbcHolder) {
        this.annotationPool = jdbcHolder.getAnnotationPool();
    }

    @Override
    public Future<AudioEntity> createAnnotation(@NonNull AudioDTO audioDTO) {
        Tuple param = audioDTO.getTuple();
        return queryOps.runQuery(AnnotationQuery.getCreateAudioData(), param, annotationPool)
                .map(res -> toEntity(audioDTO));
    }

    @Override
    public Future<List<AudioEntity>> listAnnotation(@NonNull String projectName) {
        return null;
    }

    @Override
    public Future<Void> updateAnnotation(@NonNull AudioDTO annotationDTO) {
        return null;
    }

    @Override
    public Future<Void> saveFilesMetaData(AudioDTO property) {
        return null;
    }

    @Override
    public Future<Void> deleteData(@NonNull String projectName, @NonNull String uuid) {
        return null;
    }

    @Override
    public Future<Void> createAnnotationProject() {
        return annotationPool.withConnection(connection ->
            connection.preparedQuery(AnnotationQuery.getCreateAudioProject())
                    .execute()
                    .map(DBUtils::toVoid)
                    .compose(res -> connection.preparedQuery(AnnotationQuery.getCreateWaveFormTable()).execute())
                    .map(res -> connection.close())
                    .map(DBUtils::toVoid)
            )
            .onSuccess(res -> {
                log.info("Audio project table created");
                log.info("Waveform table created");
            })
            .onFailure(res -> log.info(res.getCause().getMessage()));
    }

    @Override
    public Future<Void> deleteProjectById(@NonNull String projectId) {
        return null;
    }

    @Override
    public Future<ProjectLoaderStatus> loadAnnotationProject(@NonNull ProjectLoader projectLoader) {
        return null;
    }

    @Override
    public Future<String> renameData(@NonNull ProjectLoader projectLoader, String uuid, String newFileName) {
        return null;
    }

    @Override
    public void configProjectLoaderFromDb(@NonNull ProjectLoader loader) {

    }

    private AudioEntity toEntity(AudioDTO audioDTO) {
        return AudioEntity.builder().build();
    }

}
