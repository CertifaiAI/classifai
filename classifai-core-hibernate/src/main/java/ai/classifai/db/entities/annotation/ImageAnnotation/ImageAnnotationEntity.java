package ai.classifai.db.entities.annotation.ImageAnnotation;

import ai.classifai.db.entities.annotation.AnnotationEntity;
import ai.classifai.db.entities.dataVersion.DataVersionEntity;
import io.vertx.core.json.JsonObject;

import javax.persistence.*;

@Entity(name = "IMAGE_ANNOTATION")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class ImageAnnotationEntity extends AnnotationEntity
{
    // FIXME: inappropriate wording
    public static final String DIST_2_IMG_KEY = "distancetoImg";

    public ImageAnnotationEntity(long annotationId, DataVersionEntity dataVersionEntity, int position, String color, String label, int lineWidth)
    {
        super(annotationId, dataVersionEntity, color, label, lineWidth, position);
    }

    public ImageAnnotationEntity() {}

    protected JsonObject imageAnnotationOutputJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.put(LINE_WIDTH_KEY, getLineWidth());
        jsonObject.put(COLOR_KEY, getColor());
        jsonObject.put(LABEL_KEY, getLabel());
        jsonObject.put("id", getAnnotationKey().getAnnotationId());
        return jsonObject;
    }
}
