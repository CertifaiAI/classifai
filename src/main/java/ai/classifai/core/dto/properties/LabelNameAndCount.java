package ai.classifai.core.dto.properties;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@NonNull
public class LabelNameAndCount {
    @JsonProperty
    String label;

    @JsonProperty
    int count;
}
