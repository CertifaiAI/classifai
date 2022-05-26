package ai.classifai.dto;

import ai.classifai.dto.properties.BoundingBox;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@NonNull
public class BoundingBoxDTO {
    @JsonProperty
    String imgUuid;

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
    List<BoundingBox> boundingBoxList;

    @JsonProperty
    String imgThumbnail;

    @JsonProperty
    Integer fileSize;
}
