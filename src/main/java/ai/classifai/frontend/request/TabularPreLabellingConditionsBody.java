package ai.classifai.frontend.request;

import com.fasterxml.jackson.annotation.JsonProperty;
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
