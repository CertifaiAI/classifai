package ai.classifai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

@NonNull
@AllArgsConstructor
@Data
public class AnnotationPointProperties {
    @JsonProperty
    Integer x1;

    @JsonProperty
    Integer y1;

    @JsonProperty
    Integer x2;

    @JsonProperty
    Integer y2;

    @JsonProperty
    Integer lineWidth;

    @JsonProperty
    String color;

    @JsonProperty
    DistanceToImageProperties distancetoImg;

    @JsonProperty
    String label;

    @JsonProperty
    String id;
}