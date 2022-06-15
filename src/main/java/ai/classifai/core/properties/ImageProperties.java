package ai.classifai.core.properties;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ImageProperties {
    @JsonProperty
    String projectId;

    @JsonProperty
    String projectName;

    @JsonProperty
    String projectPath;

    @JsonProperty
    String imgUuid;

    @JsonProperty
    String imgPath;

    @JsonProperty
    int imgOriginalWidth;

    @JsonProperty
    int imgOriginalHeight;

    @JsonProperty
    int imgDepth;

    @JsonProperty
    String imgBase64;

    @JsonProperty
    Long fileSize;

}
