package ai.classifai.core.entities.dto;

import ai.classifai.db.entities.LabelEntity;
import io.vertx.core.json.JsonObject;
import lombok.*;

import java.io.Serial;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LabelDTO extends ExportDTO<LabelDTO>
{
    @Serial
    private static final long serialVersionUID = -7648864125287975561L;
    UUID id;
    String value;
    String color;
}
