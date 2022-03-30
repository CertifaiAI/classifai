package ai.classifai.dto.api.body;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.core.json.JsonObject;
import lombok.Data;

@Data
public class TabularPreLabellingConditionsBody {
    @JsonProperty("conditions")
    String conditions;

    @JsonProperty("uuid")
    String currentUuid;

    @JsonProperty("labelling_mode")
    String labellingMode;
}
