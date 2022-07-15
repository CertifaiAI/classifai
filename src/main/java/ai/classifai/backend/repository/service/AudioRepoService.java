package ai.classifai.backend.repository.service;

import ai.classifai.backend.repository.JDBCPoolHolder;
import ai.classifai.backend.repository.database.DBUtils;
import ai.classifai.backend.repository.query.AnnotationQuery;
import ai.classifai.backend.repository.query.QueryOps;
import ai.classifai.core.data.handler.ImageHandler;
import ai.classifai.core.dto.AudioDTO;
import ai.classifai.core.loader.ProjectHandler;
import ai.classifai.core.loader.ProjectLoader;
import ai.classifai.core.loader.ProjectLoaderStatus;
import ai.classifai.core.entity.annotation.AudioEntity;
import ai.classifai.core.properties.audio.AudioRegionsProperties;
import ai.classifai.core.service.annotation.AudioDataRepository;
import ai.classifai.core.utility.UuidGenerator;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonObject;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class AudioRepoService implements AudioDataRepository<AudioEntity, AudioDTO> {
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
    public Future<Void> saveFilesMetaData(AudioDTO audioDTO) {
        Tuple params = audioDTO.getTuple();
        return queryOps.runQuery(AnnotationQuery.getCreateAudioData(), params, annotationPool)
                .map(DBUtils::toVoid);
    }

    @Override
    public Future<Void> deleteData(@NonNull String projectName, @NonNull String uuid) {
        return null;
    }

    @Override
    public Future<Void> createAnnotationProject() {
        return queryOps.runQuery(AnnotationQuery.getCreateWaveFormTable(), annotationPool)
                .map(DBUtils::toVoid);
    }

    @Override
    public Future<Void> deleteProjectById(@NonNull String projectId) {
        return null;
    }

    @Override
    public Future<ProjectLoaderStatus> loadAnnotationProject(@NonNull ProjectLoader projectLoader) {
        Promise<ProjectLoaderStatus> promise = Promise.promise();
        ProjectHandler.checkProjectLoaderStatus(projectLoader)
                .onComplete(res -> {
                    if (res.succeeded()) {
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
    public void configProjectLoaderFromDb(@NonNull ProjectLoader loader) {

    }

    private AudioEntity toEntity(AudioDTO audioDTO) {
        return AudioEntity.builder().build();
    }

    @Override
    public Future<List<Integer>> getWaveFormPeaks(@NonNull ProjectLoader projectLoader) {
        Tuple params = Tuple.of(projectLoader.getProjectId());
        return queryOps.runQuery(AnnotationQuery.getRetrieveWavePeaks(), params, annotationPool)
                .map(res -> {
                    if (res.size() != 0) {
                        List<Integer> list = new ArrayList<>();
                        for (Row row : res) {
                            list.add(row.getInteger("WAVE_PEAK"));
                        }
                        return list;
                    }
                    return null;
                });
    }

    @Override
    public Future<Void> saveWaveFormPeaks(String projectId, Double timeStamp, Integer peak, String dataPath) {
        Tuple params = Tuple.from(createWaveDataPoint(projectId, timeStamp, peak, dataPath));
        return queryOps.runQuery(AnnotationQuery.getCreateWaveFormData(), params, annotationPool)
                .map(DBUtils::toVoid);
    }

    private List<Object> createWaveDataPoint(String projectId, Double timeStamp, Integer peak, String dataPath) {
        List<Object> list = new ArrayList<>();
        list.add(UuidGenerator.generateUuid());
        list.add(projectId);
        list.add(timeStamp);
        list.add(peak);
        list.add(dataPath);

        return list;
    }

    @Override
    public Future<List<AudioRegionsProperties>> getAudioRegions(@NonNull String projectId) {
        Tuple params = Tuple.of(projectId);

        return queryOps.runQuery(AnnotationQuery.getRetrieveAudioData(), params, annotationPool)
                .map(res -> {
                    List<AudioRegionsProperties> list = new ArrayList<>();
                    if (res.size() != 0) {
                        for (Row row : res) {
                            JsonObject regionJson = new JsonObject(row.getString("REGIONS_PROPS"));
                            AudioRegionsProperties regionsProperties = AudioRegionsProperties.builder()
                                    .regionId(regionJson.getString("regionId"))
                                    .labelName(regionJson.getString("labelName"))
                                    .startTime(regionJson.getDouble("startTime"))
                                    .endTime(regionJson.getDouble("endTime"))
                                    .loop(regionJson.getBoolean("loop"))
                                    .labelColor(regionJson.getString("labelColor"))
                                    .draggable(regionJson.getBoolean("draggable"))
                                    .isPlaying(regionJson.getBoolean("isPlaying"))
                                    .resizable(regionJson.getBoolean("resizable"))
                                    .build();
                            list.add(regionsProperties);
                        }
                    }
                    log.debug("Fail to retrieve audio regions");
                    return list;
                });
    }

    @Override
    public Future<Void> exportAudioAnnotation(@NonNull ProjectLoader projectLoader) {
        return null;
    }

}
