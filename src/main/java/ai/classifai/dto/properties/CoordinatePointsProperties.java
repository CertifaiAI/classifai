package ai.classifai.dto.properties;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@NonNull
public class CoordinatePointsProperties {
    @JsonProperty
    int x;

    @JsonProperty
    int y;

    @JsonProperty
    DistanceToImageProperties distanceToImageProperties;
}
