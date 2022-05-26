package ai.classifai.dto.properties;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@NonNull
public class Segmentation {
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
