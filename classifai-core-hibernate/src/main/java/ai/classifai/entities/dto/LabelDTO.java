package ai.classifai.entities.dto;

import ai.classifai.database.model.Label;
import ai.classifai.database.model.Version;
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
    private static final long serialVersionUID = -4883616821114298778L;

    UUID id;
    String value;
    String color;

    @Override
    public LabelDTO readJson(String jsonString)
    {
        JsonObject json = new JsonObject(jsonString);

        return LabelDTO.builder()
                .id(UUID.fromString(json.getString(Label.LABEL_ID_KEY)))
                .value(json.getString(Label.VALUE_KEY))
                .color(json.getString(Label.COLOR_KEY))
                .build();
    }

    @Override
    public String toJson()
    {
        return new JsonObject()
                .put(Label.LABEL_ID_KEY, id.toString())
                .put(Label.VALUE_KEY, value)
                .put(Label.COLOR_KEY, color)
                .encodePrettily();
    }
}
