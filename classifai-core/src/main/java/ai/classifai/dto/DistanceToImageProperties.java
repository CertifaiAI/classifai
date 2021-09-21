package ai.classifai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@NonNull
@Builder
@Data
public class DistanceToImageProperties {
    @JsonProperty
    Integer x;

    @JsonProperty
    Integer y;
}
