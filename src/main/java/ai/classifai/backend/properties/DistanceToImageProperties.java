package ai.classifai.backend.dto.properties;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@NonNull
public class DistanceToImageProperties {
    @JsonProperty
    int x;

    @JsonProperty
    int y;
}
