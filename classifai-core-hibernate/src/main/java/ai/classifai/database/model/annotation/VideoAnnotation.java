package ai.classifai.database.model.annotation;

import io.vertx.core.json.JsonObject;

import javax.persistence.Entity;

//@Entity
public class VideoAnnotation extends Annotation
{
    @Override
    public JsonObject outputJson() {
        return null;
    }
}
