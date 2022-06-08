package ai.classifai.backend.repository.service;

import ai.classifai.backend.repository.JdbcHolder;
import ai.classifai.core.dto.BoundingBoxDTO;
import ai.classifai.core.entity.annotation.ImageBoundingBoxEntity;
import ai.classifai.core.service.annotation.AnnotationRepository;
import io.vertx.core.Future;
import io.vertx.jdbcclient.JDBCPool;
import lombok.NonNull;

import java.util.List;
import java.util.Optional;

public class ImageBoundingBoxRepoService implements AnnotationRepository<ImageBoundingBoxEntity, BoundingBoxDTO> {
    private final JDBCPool annotationPool;

    public ImageBoundingBoxRepoService(JdbcHolder jdbcHolder) {
        this.annotationPool = jdbcHolder.getAnnotationPool();
    }

    @Override
    public Future<ImageBoundingBoxEntity> createAnnotation(@NonNull BoundingBoxDTO annotationDTO) {
        return null;
    }

    @Override
    public Future<List<ImageBoundingBoxEntity>> listAnnotation() {
        return null;
    }

    @Override
    public Future<ImageBoundingBoxEntity> updateAnnotation(@NonNull BoundingBoxDTO annotationDTO) {
        return null;
    }

    @Override
    public Future<Optional<ImageBoundingBoxEntity>> getAnnotationById(@NonNull String id) {
        return null;
    }

    @Override
    public Future<Void> deleteProjectById(@NonNull BoundingBoxDTO annotationDTO) {
        return null;
    }

    @Override
    public ImageBoundingBoxEntity toAnnotationEntity(@NonNull BoundingBoxDTO annotationDTO) {
        return null;
    }
}
