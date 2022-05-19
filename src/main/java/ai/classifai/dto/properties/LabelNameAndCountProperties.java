package ai.classifai.dto.properties;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@NonNull
public class LabelNameAndCountProperties {
    @JsonProperty
    String label;

    @JsonProperty
    Integer count;
}
