package ai.classifai.backend.repository.service;

import ai.classifai.backend.repository.JDBCPoolHolder;
import ai.classifai.backend.repository.database.DBUtils;
import ai.classifai.backend.repository.query.AnnotationQuery;
import ai.classifai.backend.repository.query.QueryOps;
import ai.classifai.core.dto.VideoDTO;
import ai.classifai.core.entity.annotation.VideoEntity;
import ai.classifai.core.loader.ProjectLoader;
import ai.classifai.core.loader.ProjectLoaderStatus;
import ai.classifai.core.service.annotation.AnnotationRepository;
import ai.classifai.frontend.request.ThumbnailProperties;
import io.vertx.core.Future;
import io.vertx.jdbcclient.JDBCPool;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class VideoRepoService implements AnnotationRepository<VideoEntity, VideoDTO> {
    private final JDBCPool annotationPool;
    private final QueryOps queryOps = new QueryOps();

    public VideoRepoService(JDBCPoolHolder jdbcPoolHolder) {
        this.annotationPool = jdbcPoolHolder.getAnnotationPool();
    }

    @Override
    public Future<VideoEntity> createAnnotation(@NonNull VideoDTO annotationDTO) throws Exception {
        return null;
    }

    @Override
    public Future<List<VideoEntity>> listAnnotation(@NonNull String projectName) {
        return null;
    }

    @Override
    public Future<Void> updateAnnotation(@NonNull VideoDTO annotationDTO) {
        return null;
    }

    @Override
    public Future<Void> saveFilesMetaData(VideoDTO property) {
        return null;
    }

    @Override
    public Future<Void> deleteData(@NonNull String projectName, @NonNull String uuid) {
        return null;
    }

    @Override
    public Future<Void> createAnnotationProject() {
        return queryOps.runQuery(AnnotationQuery.getCreateVideoProject(), annotationPool)
                .map(DBUtils::toVoid);
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

}
