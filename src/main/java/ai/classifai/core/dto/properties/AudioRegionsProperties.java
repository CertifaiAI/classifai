package ai.classifai.core.dto.properties;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@NonNull
public class AudioRegionsProperties {
    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String regionId;

    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String labelName;

    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_NULL)
    double startTime;

    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_NULL)
    double endTime;

    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_NULL)
    boolean loop;

    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String labelColor;

    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_NULL)
    boolean draggable;

    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_NULL)
    boolean resizable;

    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_NULL)
    boolean isPlaying;
}
