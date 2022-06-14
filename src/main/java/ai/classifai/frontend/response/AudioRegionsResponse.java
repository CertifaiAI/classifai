package ai.classifai.frontend.response;

import ai.classifai.core.dto.properties.AudioRegionsProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AudioRegionsResponse {
    @JsonProperty
    int message;

    @JsonProperty("error_message")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String errorMessage;

    @JsonProperty("audio_regions")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    List<AudioRegionsProperties> listOfRegions;
}
