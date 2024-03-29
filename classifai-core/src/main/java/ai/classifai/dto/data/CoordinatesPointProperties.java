package ai.classifai.dto.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@NonNull
public class CoordinatesPointProperties {
    @JsonProperty
    int x;

    @JsonProperty
    int y;

    @JsonProperty
    DistanceToImageProperties distancetoImg;
}
