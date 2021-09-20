package ai.classifai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class DistanceToImageProperties {
    @JsonProperty
    Integer x;

    @JsonProperty
    Integer y;
}
