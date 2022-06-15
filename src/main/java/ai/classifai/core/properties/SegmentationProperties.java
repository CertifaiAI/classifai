package ai.classifai.core.properties;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NonNull
public class SegmentationProperties {
    @JsonProperty
    String uuid;

    @JsonProperty
    int x;

    @JsonProperty
    int y;

    @JsonProperty
    String label;

    @JsonProperty
    List<String> subLabel;

    @JsonProperty
    String color;

    @JsonProperty
    String lineWidth;
}
