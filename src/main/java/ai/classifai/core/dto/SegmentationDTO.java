package ai.classifai.core.dto;

import ai.classifai.core.dto.properties.SegmentationProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@NonNull
public class SegmentationDTO {
    @JsonProperty
    String uuid;

    @JsonProperty
    String imgPath;

    @JsonProperty("img_x")
    int imgX;

    @JsonProperty("img_y")
    int imgY;

    @JsonProperty("img_w")
    int imgW;

    @JsonProperty("img_h")
    int imgH;

    @JsonProperty("img_ori_w")
    int imgOriginalWidth;

    @JsonProperty("img_ori_h")
    int imgOriginalHeight;

    @JsonProperty
    List<SegmentationProperties> segmentationPropertiesList;

    @JsonProperty
    String imgThumbnail;

    @JsonProperty
    Integer fileSize;
}
