package ai.classifai.core.entities.dto.annotation.image;

import ai.classifai.db.entities.VersionEntity;
import ai.classifai.db.entities.annotation.AnnotationEntity;
import ai.classifai.db.entities.annotation.ImageAnnotation.BoundingBoxAnnotationEntity;
import ai.classifai.db.entities.annotation.ImageAnnotation.Point;
import ai.classifai.db.entities.data.DataEntity;
import io.vertx.core.json.JsonObject;
import lombok.*;

import java.io.Serial;

@lombok.Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BoundingBoxDTO extends ImageAnnotationDTO
{
    @Serial
    private static final long serialVersionUID = -7680600888104516111L;

    private float x1;
    private float x2;
    private float y1;
    private float y2;
    private float dist2ImgX;
    private float dist2ImgY;

    @Builder
    public BoundingBoxDTO(long annotaionId, String dataId, String versionId, int position, float x1, float x2,
                          float y1, float y2, float dist2ImgX, float dist2ImgY)
    {
        super(annotaionId, dataId, versionId, position);
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;
        this.dist2ImgX = dist2ImgX;
        this.dist2ImgY = dist2ImgY;
    }

    @Override
    public BoundingBoxDTO readJson(String jsonString)
    {
        JsonObject json = new JsonObject(jsonString);

        JsonObject dist2Img = json.getJsonObject(BoundingBoxAnnotationEntity.DIST_2_IMG_KEY);

        return BoundingBoxDTO.builder()
                .annotaionId(json.getLong(AnnotationEntity.ANNOTATION_ID_KEY))
                .dataId(json.getString(DataEntity.DATA_ID_KEY))
                .versionId(json.getString(VersionEntity.VERSION_ID_KEY))
                .x1(json.getFloat(BoundingBoxAnnotationEntity.X1_KEY))
                .x2(json.getFloat(BoundingBoxAnnotationEntity.X2_KEY))
                .y1(json.getFloat(BoundingBoxAnnotationEntity.Y1_KEY))
                .y2(json.getFloat(BoundingBoxAnnotationEntity.Y2_KEY))
                .dist2ImgX(dist2Img.getFloat(Point.X))
                .dist2ImgY(dist2Img.getFloat(Point.Y))
                .build();
    }

    @Override
    public String toJson()
    {
        JsonObject dist2Img = new JsonObject()
                .put(Point.X, dist2ImgX)
                .put(Point.Y, dist2ImgY);

        return new JsonObject()
                .put(AnnotationEntity.ANNOTATION_ID_KEY, annotationId)
                .put(DataEntity.DATA_ID_KEY, dataId)
                .put(VersionEntity.VERSION_ID_KEY, versionId)
                .put(BoundingBoxAnnotationEntity.X1_KEY, x1)
                .put(BoundingBoxAnnotationEntity.X2_KEY, x2)
                .put(BoundingBoxAnnotationEntity.Y1_KEY, y1)
                .put(BoundingBoxAnnotationEntity.Y2_KEY, y2)
                .put(BoundingBoxAnnotationEntity.DIST_2_IMG_KEY, dist2Img)
                .encodePrettily();
    }
}
