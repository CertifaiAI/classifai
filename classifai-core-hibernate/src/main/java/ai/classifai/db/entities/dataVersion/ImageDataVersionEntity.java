package ai.classifai.db.entities.dataVersion;

import ai.classifai.core.entities.Annotation;
import ai.classifai.core.entities.dto.dataversion.DataVersionDTO;
import ai.classifai.db.entities.VersionEntity;
import ai.classifai.db.entities.annotation.AnnotationEntity;
import ai.classifai.db.entities.data.DataEntity;
import ai.classifai.util.type.AnnotationHandler;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.List;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "IMAGE_DATA_VERSION")
public class ImageDataVersionEntity extends DataVersionEntity
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

    @Override
    public DataVersionDTO toDTO() {
        return null;
    }
}
