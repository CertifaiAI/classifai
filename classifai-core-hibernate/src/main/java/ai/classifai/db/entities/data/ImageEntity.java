package ai.classifai.db.entities.data;

import ai.classifai.core.entities.dto.DataDTO;
import ai.classifai.db.entities.ProjectEntity;
import ai.classifai.db.entities.dataVersion.ImageDataVersionEntity;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.UUID;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "IMAGE")
public class ImageEntity extends DataEntity
{
    @Column(name = "img_depth")
    private int depth;

    @Column(name = "img_width")
    private int width;

    @Column(name = "img_height")
    private int height;
}
