package ai.classifai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.util.List;

@NonNull
@Builder
@Data
public class DataInfoProperties {

    @JsonProperty("annotation")
    List<AnnotationPointProperties> annotation;

    @JsonProperty("img_x")
    int imgX;

    @JsonProperty("img_y")
    int imgY;

    @JsonProperty("img_w")
    int imgW;

    @JsonProperty("img_h")
    int imgH;
}
