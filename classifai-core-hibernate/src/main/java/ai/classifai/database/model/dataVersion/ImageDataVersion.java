package ai.classifai.database.model.dataVersion;

import ai.classifai.database.model.Version;
import ai.classifai.database.model.annotation.Annotation;
import ai.classifai.database.model.data.Data;
import ai.classifai.util.type.AnnotationHandler;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "IMAGE_DATA_VERSION")
public class ImageDataVersion extends DataVersion
{
    public static final String IMG_X_KEY = "img_x";
    public static final String IMG_Y_KEY = "img_y";
    public static final String IMG_W_KEY = "img_w";
    public static final String IMG_H_KEY = "img_h";

    @Column(name = IMG_X_KEY)
    private float imgX;

    @Column(name = IMG_Y_KEY)
    private float imgY;

    @Column(name = IMG_W_KEY)
    private float imgW;

    @Column(name = IMG_H_KEY)
    private float imgH;

    public ImageDataVersion(Data data, Version version)
    {
        super(data, version);
        imgX = 0;
        imgY = 0;
        imgW = 0;
        imgH = 0;
    }

    public ImageDataVersion() {}

    @Override
    protected void updateDataFromJsonImplementation(JsonObject request) {
        imgX = request.getFloat(IMG_X_KEY);
        imgY = request.getFloat(IMG_Y_KEY);
        imgW = request.getFloat(IMG_W_KEY);
        imgH = request.getFloat(IMG_H_KEY);
    }

    @Override
    public JsonObject outputJson() {
        JsonObject jsonObj = new JsonObject();
        jsonObj.put(IMG_X_KEY, imgX);
        jsonObj.put(IMG_Y_KEY, imgY);
        jsonObj.put(IMG_W_KEY, imgW);
        jsonObj.put(IMG_H_KEY, imgH);
        jsonObj.put(AnnotationHandler.getMetaKey(getData().getProject().getAnnoType()),
                Annotation.getAnnotationJsonList(getAnnotations()));

        return jsonObj;
    }
}
