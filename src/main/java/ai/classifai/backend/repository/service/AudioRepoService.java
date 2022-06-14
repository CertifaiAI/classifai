package ai.classifai.backend.repository.service;

import ai.classifai.backend.repository.DBUtils;
import ai.classifai.backend.repository.JdbcHolder;
import ai.classifai.backend.repository.QueryOps;
import ai.classifai.backend.repository.SqlQueries;
import ai.classifai.core.dto.AudioDTO;
import ai.classifai.core.dto.properties.AudioProperties;
import ai.classifai.core.entity.annotation.AudioEntity;
import ai.classifai.core.service.annotation.AnnotationRepository;
import io.vertx.core.Future;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.Tuple;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class AudioRepoService implements AnnotationRepository<AudioEntity, AudioDTO, AudioProperties> {
    private final JDBCPool annotationPool;
    private final QueryOps queryOps = new QueryOps();

    public AudioRepoService(JdbcHolder jdbcHolder) {
        this.annotationPool = jdbcHolder.getAnnotationPool();
    }

    private AudioEntity toEntity(AudioDTO audioDTO) {
        return AudioEntity.builder()
                .build();
    }

    @Override
    public Future<AudioEntity> createAnnotation(@NonNull AudioDTO audioDTO) {
        Tuple param = audioDTO.getTuple();
        return queryOps.runQuery(SqlQueries.getCreateAudioData(), param, annotationPool)
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
    public Future<Void> saveFilesMetaData(AudioProperties property) {
        return null;
    }

    @Override
    public Future<Void> deleteData(@NonNull String projectName, @NonNull String uuid) {
        return null;
    }

    @Override
    public Future<Void> createAnnotationProject() {
        return annotationPool.withConnection(connection ->
            connection.preparedQuery(SqlQueries.getCreateAudioProject())
                    .execute()
                    .map(DBUtils::toVoid)
                    .compose(res -> connection.preparedQuery(SqlQueries.getCreateWaveFormTable()).execute())
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
    public Future<Void> deleteProjectByName(@NonNull String projectName) {
        return null;
    }
}
