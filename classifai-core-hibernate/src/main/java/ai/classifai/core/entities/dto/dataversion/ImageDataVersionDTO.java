package ai.classifai.core.entities.dto.dataversion;

import ai.classifai.db.entities.VersionEntity;
import ai.classifai.db.entities.data.DataEntity;
import ai.classifai.db.entities.dataVersion.ImageDataVersionEntity;
import io.vertx.core.json.JsonObject;
import lombok.*;

import java.io.Serial;

@lombok.Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ImageDataVersionDTO extends DataVersionDTO
{

    @Serial
    private static final long serialVersionUID = 1357120222631740426L;

    private float imgX;
    private float imgY;
    private float imgW;
    private float imgH;

}
