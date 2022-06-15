package ai.classifai.core.properties;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BoundingBoxProperties {
    @JsonProperty
    String uuid;

    @JsonProperty
    int x1;

    @JsonProperty
    int y1;

    @JsonProperty
    int x2;

    @JsonProperty
    int y2;

    @JsonProperty
    DistanceToImageProperties distanceToImageProperties;

    @JsonProperty
    String label;

    @JsonProperty
    List<String> subLabel;

    @JsonProperty
    String color;

    @JsonProperty
    String lineWidth;
}
