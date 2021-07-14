package ai.classifai.entities.dto;

import ai.classifai.database.model.Version;
import io.vertx.core.json.JsonObject;
import lombok.*;

import java.io.Serial;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class VersionDTO extends ExportDTO<VersionDTO>
{
    @Serial
    private static final long serialVersionUID = 2372213803175062452L;

    UUID id;
    LocalDateTime createdDate;
    LocalDateTime lastModifiedDate;

    List<UUID> labelIds;

    @Override
    public String toJson()
    {
        return new JsonObject()
                .put(Version.VERSION_ID_KEY, id.toString())
                .encodePrettily();
    }

    @Override
    public VersionDTO readJson(String jsonString)
    {
        JsonObject json = new JsonObject(jsonString);

        return VersionDTO.builder()
                .id(UUID.fromString(json.getString(Version.VERSION_ID_KEY)))
                .build();
    }
}
