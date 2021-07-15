package ai.classifai.core.entities.dto;

import ai.classifai.db.entities.data.DataEntity;
import io.vertx.core.json.JsonObject;
import lombok.*;

import java.io.Serial;
import java.util.UUID;

@lombok.Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DataDTO extends ExportDTO<DataDTO>
{
    @Serial
    private static final long serialVersionUID = -525152795070462691L;

    UUID id;
    String path;
    String checksum;
    Long fileSize;

}
