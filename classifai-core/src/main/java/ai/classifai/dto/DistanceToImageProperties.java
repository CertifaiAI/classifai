package ai.classifai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

@NonNull
@AllArgsConstructor
@Data
public class DistanceToImageProperties {
    @JsonProperty
    Integer x;

    @JsonProperty
    Integer y;
}
