package ai.classifai.database.model.annotation.ImageAnnotation;

import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serial;
import java.io.Serializable;

// FIXME:
//  abstract out for every image
@Getter
@Setter
@Embeddable
public class Point implements Serializable
{
    public static final String X = "x";
    public static final String Y = "y";

    @Serial
    private static final long serialVersionUID = 8279646809856034746L;

    @Column(name = X)
    private float x;

    @Column(name = Y)
    private float y;

    public Point() {}

    public Point(float x, float y)
    {
        this.x = x;
        this.y = y;
    }

    public JsonObject outputJson()
    {
        JsonObject jsonObj = new JsonObject();
        jsonObj.put(X, x);
        jsonObj.put(Y, y);
        return jsonObj;
    }
}
