package ai.classifai.core.dto.properties;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AudioProperties {
    @JsonProperty
    String regionId;

    @JsonProperty
    String labelName;

    @JsonProperty
    double startTime;

    @JsonProperty
    double endTime;

    @JsonProperty
    boolean loop;

    @JsonProperty
    String labelColor;

    @JsonProperty
    boolean draggable;

    @JsonProperty
    boolean resizable;

    @JsonProperty
    boolean isPlaying;
}
