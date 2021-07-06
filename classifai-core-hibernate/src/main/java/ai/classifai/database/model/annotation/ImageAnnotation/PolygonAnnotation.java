package ai.classifai.database.model.annotation.ImageAnnotation;

import io.vertx.core.json.JsonObject;

import javax.persistence.Entity;

@Entity(name = "POLYGON_ANNOTATION")
public class PolygonAnnotation extends ImageAnnotation
{
    @Override
    public JsonObject outputJson() {
        return null;
    }
}
