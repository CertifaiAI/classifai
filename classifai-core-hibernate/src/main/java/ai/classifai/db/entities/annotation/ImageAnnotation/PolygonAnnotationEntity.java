package ai.classifai.db.entities.annotation.ImageAnnotation;

import io.vertx.core.json.JsonObject;

import javax.persistence.Entity;

@Entity(name = "POLYGON_ANNOTATION")
public class PolygonAnnotationEntity extends ImageAnnotationEntity
{
    @Override
    public JsonObject outputJson() {
        return null;
    }
}
