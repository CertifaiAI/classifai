package ai.classifai.database.model.annotation.ImageAnnotation;

import ai.classifai.database.model.annotation.Annotation;
import ai.classifai.database.model.dataVersion.DataVersion;
import io.vertx.core.json.JsonObject;

import javax.persistence.*;

@Entity(name = "IMAGE_ANNOTATION")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class ImageAnnotation extends Annotation
{
    public static final String DIST_2_IMG_KEY = "distancetoImg";
    public static final String COLOR_KEY = "color";
    public static final String LABEL_KEY = "label";
    public static final String LINE_WIDTH_KEY = "lineWidth";

    @Column(name = COLOR_KEY)
    private String color;

    @Column(name = LABEL_KEY)
    private String label;

    @Column(name = LINE_WIDTH_KEY)
    private int lineWidth;

    public ImageAnnotation(long annotationId, DataVersion dataVersion, int position, String color, String label, int lineWidth)
    {
        super(annotationId, dataVersion, position);
        this.color = color;
        this.label = label;
        this.lineWidth = lineWidth;
    }

    protected JsonObject imageAnnotationOutputJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.put(LINE_WIDTH_KEY, lineWidth);
        jsonObject.put(COLOR_KEY, color);
        jsonObject.put(LABEL_KEY, label);
        jsonObject.put("id", getAnnotationKey().getAnnotationId());
        return jsonObject;
    }

    public ImageAnnotation() {}
}
