package ai.classifai.dto;

import ai.classifai.dto.properties.Segmentation;
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
    List<Segmentation> segmentationList;

    @JsonProperty
    String imgThumbnail;

    @JsonProperty
    Integer fileSize;
}
