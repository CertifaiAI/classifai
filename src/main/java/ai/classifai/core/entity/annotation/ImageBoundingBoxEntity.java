package ai.classifai.core.entity.annotation;

import ai.classifai.core.dto.properties.BoundingBoxProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ImageBoundingBoxEntity {
    @JsonProperty
    String imgUuid;

    @JsonProperty
    String imgPath;

    @JsonProperty("img_x")
    Integer imgX;

    @JsonProperty("img_y")
    Integer imgY;

    @JsonProperty("img_w")
    Integer imgW;

    @JsonProperty("img_h")
    Integer imgH;

    @JsonProperty("img_ori_w")
    Integer imgOriginalWidth;

    @JsonProperty("img_ori_h")
    Integer imgOriginalHeight;

    @JsonProperty
    List<BoundingBoxProperties> boundingBoxPropertiesList;

    @JsonProperty
    String imgThumbnail;

    @JsonProperty
    Integer fileSize;

}
