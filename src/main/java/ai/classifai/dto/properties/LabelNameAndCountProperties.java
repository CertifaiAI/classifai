package ai.classifai.dto.properties;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LabelNameAndCountProperties {
    @JsonProperty
    String label;

    @JsonProperty
    Integer count;
}
