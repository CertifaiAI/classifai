package ai.classifai.backend.repository.service;

import ai.classifai.backend.repository.database.DBUtils;
import ai.classifai.backend.repository.JdbcHolder;
import ai.classifai.backend.repository.query.QueryOps;
import ai.classifai.backend.repository.SqlQueries;
import ai.classifai.core.dto.BoundingBoxDTO;
import ai.classifai.core.properties.BoundingBoxProperties;
import ai.classifai.core.properties.ImageProperties;
import ai.classifai.core.entity.annotation.ImageBoundingBoxEntity;
import ai.classifai.core.service.annotation.AnnotationRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.Future;
import io.vertx.jdbcclient.JDBCPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ImageBoundingBoxRepoService implements AnnotationRepository<ImageBoundingBoxEntity, BoundingBoxDTO, ImageProperties> {
    private final JDBCPool annotationPool;
    private final QueryOps queryOps = new QueryOps();

    public ImageBoundingBoxRepoService(JdbcHolder jdbcHolder) {
        this.annotationPool = jdbcHolder.getAnnotationPool();
    }

    private ImageBoundingBoxEntity toEntity(ImageProperties imageProperties) {
        return ImageBoundingBoxEntity.builder()
                .projectId(imageProperties.getProjectId())
                .projectName(imageProperties.getProjectName())
                .imgUuid(imageProperties.getImgUuid())
                .imgPath(imageProperties.getImgPath())
                .imgDepth(imageProperties.getImgDepth())
                .imgOriginalWidth(imageProperties.getImgOriginalWidth())
                .imgOriginalHeight(imageProperties.getImgOriginalHeight())
                .fileSize(imageProperties.getFileSize())
                .imgBase64(imageProperties.getImgBase64())
                .build();
    }

    private ImageBoundingBoxEntity toEntity(BoundingBoxDTO boundingBoxDTO) {
        return ImageBoundingBoxEntity.builder()
                .projectName(boundingBoxDTO.getProjectName())
                .imgUuid(boundingBoxDTO.getImgUuid())
                .imgDepth(boundingBoxDTO.getImgDepth())
                .imgOriginalWidth(boundingBoxDTO.getImgOriginalWidth())
                .imgOriginalHeight(boundingBoxDTO.getImgOriginalHeight())
                .fileSize(boundingBoxDTO.getFileSize())
                .imgBase64(boundingBoxDTO.getImgBase64())
                .build();
    }

    @Override
    public Future<Void> createAnnotationProject() {
        return queryOps.runQuery(SqlQueries.getCreateImageProject(), annotationPool)
                .map(DBUtils::toVoid);
    }

    @Override
    public Future<Void> saveFilesMetaData(ImageProperties properties) {
        Tuple params = toEntity(properties).getTuple();
        return queryOps.runQuery(SqlQueries.getCreateData(), params, annotationPool)
                .map(DBUtils::toVoid);
    }

    @Override
    public Future<Void> deleteData(@NonNull String projectName, @NonNull String uuid) {
        Tuple params = Tuple.of(projectName, uuid);
        return queryOps.runQuery(SqlQueries.getDeleteImageProjectData(), params, annotationPool)
                .map(DBUtils::toVoid);
    }

    @Override
    public Future<ImageBoundingBoxEntity> createAnnotation(@NonNull BoundingBoxDTO boundingBoxDTO) throws JsonProcessingException {
        ImageBoundingBoxEntity entity = toEntity(boundingBoxDTO);
        String boundingBoxPropertiesString = writeJsonString(entity);
        Tuple params = Tuple.of(boundingBoxPropertiesString);
        return queryOps.runQuery(SqlQueries.getUpdateImageData(), params, annotationPool)
                .map(res -> entity);
    }

    @Override
    public Future<List<ImageBoundingBoxEntity>> listAnnotation(@NonNull String projectName) {
        Tuple param = Tuple.of(projectName);
        return queryOps.runQuery(SqlQueries.getRetrieveImageProjectByName(), param, annotationPool)
                .map(res -> {
                    if (res.size() != 0) {
                        List<ImageBoundingBoxEntity> list = new ArrayList<>();
                        for (Row row : res.value()) {
                            try {
                                ImageBoundingBoxEntity entity = ImageBoundingBoxEntity.builder()
                                        .projectName(row.getString("PROJECT_NAME"))
                                        .imgUuid(row.getString("IMG_UUID"))
                                        .imgOriginalHeight(row.getInteger("IMG_ORI_HEIGHT"))
                                        .imgOriginalWidth(row.getInteger("IMG_ORI_WIDTH"))
                                        .imgDepth(row.getInteger("IMG_DEPTH"))
                                        .imgBase64(row.getString("IMG_THUMBNAIL"))
                                        .imgX(row.getInteger("IMG_X"))
                                        .imgY(row.getInteger("IMG_Y"))
                                        .imgH(row.getInteger("IMG_H"))
                                        .imgW(row.getInteger("IMG_W"))
                                        .fileSize(row.getLong("FILE_SIZE"))
                                        .boundingBoxPropertiesList(parseBoundingBoxProperties(row.getString("BND_BOX")))
                                        .build();

                                list.add(entity);
                            } catch (JsonProcessingException exception) {
                                log.info(exception.getMessage());
                            }
                        }
                        return list;
                    }
                    log.info("Failed to retrieve annotation data for project " + projectName);
                    return null;
                });
    }

    @Override
    public Future<Void> updateAnnotation(@NonNull BoundingBoxDTO annotationDTO) throws JsonProcessingException {
        String boundingBoxPropertiesString = writeJsonString(annotationDTO.getBoundingBoxPropertiesList());
        Tuple params = Tuple.of(boundingBoxPropertiesString, annotationDTO.getImgUuid(), annotationDTO.getProjectName());
        return queryOps.runQuery(SqlQueries.getUpdateImageData(), params, annotationPool)
                .map(DBUtils::toVoid);
    }

    @Override
    public Future<Void> deleteProjectByName(@NonNull String projectName) {
        Tuple params = Tuple.of(projectName);
        return queryOps.runQuery(SqlQueries.getDeleteImageProjectData(), params, annotationPool)
                .map(DBUtils::toVoid);
    }

    private List<BoundingBoxProperties> parseBoundingBoxProperties(String jsonList) throws JsonProcessingException {
        return new ObjectMapper().readValue(jsonList, new TypeReference<>() {});
    }

    private String writeJsonString(Object object) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(object);
    }
}
