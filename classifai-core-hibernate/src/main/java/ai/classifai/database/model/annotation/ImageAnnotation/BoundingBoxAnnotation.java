package ai.classifai.database.model.annotation.ImageAnnotation;

import ai.classifai.database.model.dataVersion.DataVersion;
import io.vertx.core.json.JsonObject;

import javax.persistence.*;

@Entity(name = "BOUNDING_BOX_ANNOTATION")
public class BoundingBoxAnnotation extends ImageAnnotation
{
    public static final String META_KEY = "bnd_box";
    public static final String X1_KEY = "x1";
    public static final String X2_KEY = "x2";
    public static final String Y1_KEY = "y1";
    public static final String Y2_KEY = "y2";


    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = Point.X, column = @Column(name = X1_KEY)),
            @AttributeOverride(name = Point.Y, column = @Column(name = Y1_KEY))
    })
    private Point topLeft;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = Point.X, column = @Column(name = X2_KEY)),
            @AttributeOverride(name = Point.Y, column = @Column(name = Y2_KEY))
    })
    private Point botRight;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = Point.X, column = @Column(name = "dist2ImgX")),
            @AttributeOverride(name = Point.Y, column = @Column(name = "dist2ImgY"))
    })
    private Point dist2Img;

    public BoundingBoxAnnotation(long annotationId, DataVersion dataVersion, int position, String color, String label, int lineWidth, float x1, float x2,
                                 float y1, float y2, float dist2ImgX, float dist2ImgY)
    {
        super(annotationId, dataVersion, position, color, label, lineWidth);
        topLeft = new Point(x1, y1);
        botRight = new Point(x2, y2);
        dist2Img = new Point(dist2ImgX, dist2ImgY);
    }

    public BoundingBoxAnnotation() {}

    @Override
    public JsonObject outputJson() {
        JsonObject jsonObj = new JsonObject();
        jsonObj.put(X1_KEY, topLeft.getX());
        jsonObj.put(Y1_KEY, topLeft.getY());
        jsonObj.put(X2_KEY, botRight.getX());
        jsonObj.put(Y2_KEY, botRight.getY());
        jsonObj.put(DIST_2_IMG_KEY, dist2Img.outputJson());
        jsonObj.mergeIn(imageAnnotationOutputJson());
        return jsonObj;
    }

    public static BoundingBoxAnnotation getAnnotationFromJson(JsonObject jsonObject, DataVersion dataVersion, int position)
    {
        float x1 = jsonObject.getFloat(X1_KEY);
        float x2 = jsonObject.getFloat(X2_KEY);
        float y1 = jsonObject.getFloat(Y1_KEY);
        float y2 = jsonObject.getFloat(Y2_KEY);

        JsonObject dist2Img = jsonObject.getJsonObject(DIST_2_IMG_KEY);
        float dist2ImgX = dist2Img.getFloat(Point.X);
        float dist2ImgY = dist2Img.getFloat(Point.Y);

        int lineWidth = jsonObject.getInteger(LINE_WIDTH_KEY);
        long id = jsonObject.getLong( "id");
        String label = jsonObject.getString(LABEL_KEY);
        String color = jsonObject.getString(COLOR_KEY);

        return new BoundingBoxAnnotation(id, dataVersion, position, color, label, lineWidth, x1, x2, y1, y2, dist2ImgX, dist2ImgY);
    }
}
