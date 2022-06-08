package ai.classifai.core.dto.properties;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NonNull;

@Data
@NonNull
public class ImageProperties {
    @JsonProperty
    String imgUuid;

    @JsonProperty
    String imgPath;

    @JsonProperty
    Integer imgDepth;

    @JsonProperty
    Integer imgX;

    @JsonProperty
    Integer imgY;

    @JsonProperty
    Integer imgW;

    @JsonProperty
    Integer imgH;

    @JsonProperty
    Integer imgOriginalHeight;

    @JsonProperty
    Integer imgOriginalWidth;

    @JsonProperty
    Integer fileSize;

    @JsonProperty
    String imageThumbnail;
}
