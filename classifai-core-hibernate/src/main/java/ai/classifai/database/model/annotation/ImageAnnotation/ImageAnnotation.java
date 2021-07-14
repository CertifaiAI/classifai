package ai.classifai.database.model.annotation.ImageAnnotation;

import ai.classifai.database.model.annotation.Annotation;
import ai.classifai.database.model.dataVersion.DataVersion;
import io.vertx.core.json.JsonObject;

import javax.persistence.*;

@Entity(name = "IMAGE_ANNOTATION")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class ImageAnnotation extends Annotation
{
    // FIXME: inappropriate wording
    public static final String DIST_2_IMG_KEY = "distancetoImg";

    public ImageAnnotation(long annotationId, DataVersion dataVersion, int position, String color, String label, int lineWidth)
    {
        super(annotationId, dataVersion, color, label, lineWidth, position);
    }

    public ImageAnnotation() {}

    protected JsonObject imageAnnotationOutputJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.put(LINE_WIDTH_KEY, getLineWidth());
        jsonObject.put(COLOR_KEY, getColor());
        jsonObject.put(LABEL_KEY, getLabel());
        jsonObject.put("id", getAnnotationKey().getAnnotationId());
        return jsonObject;
    }
}
