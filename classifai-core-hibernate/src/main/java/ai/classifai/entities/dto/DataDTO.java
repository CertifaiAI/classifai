package ai.classifai.entities.dto;

import ai.classifai.database.model.data.Data;
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

    @Override
    public DataDTO readJson(String jsonString)
    {
        JsonObject json = new JsonObject(jsonString);

        return DataDTO.builder()
                .id(UUID.fromString(json.getString(Data.DATA_ID_KEY)))
                .checksum(json.getString(Data.CHECKSUM_KEY))
                .path(json.getString(Data.DATA_PATH_KEY))
                .build();
    }

    @Override
    public String toJson()
    {
        return new JsonObject()
                .put(Data.DATA_ID_KEY, id.toString())
                .put(Data.CHECKSUM_KEY, checksum)
                .put(Data.DATA_PATH_KEY, path)
                .encodePrettily();
    }
}
