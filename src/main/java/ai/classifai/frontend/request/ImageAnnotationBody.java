package ai.classifai.frontend.request;

import ai.classifai.core.properties.BoundingBoxProperties;
import ai.classifai.core.properties.SegmentationProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ImageAnnotationBody {
    @JsonProperty("project_name")
    String projectName;

    @JsonProperty("img_uuid")
    String imgUuid;

    @JsonProperty("img_x")
    int imgX;

    @JsonProperty("img_y")
    int imgY;

    @JsonProperty("img_w")
    int imgW;

    @JsonProperty("img_h")
    int imgH;

    @JsonProperty("img_depth")
    int imgDepth;

    @JsonProperty("img_ori_w")
    int imgOriginalWidth;

    @JsonProperty("img_ori_h")
    int imgOriginalHeight;

    @JsonProperty("bnd_box")
    List<BoundingBoxProperties> boundingBoxPropertiesList;

    @JsonProperty("seg")
    List<SegmentationProperties> segmentationPropertiesList;

    @JsonProperty("img_thumbnail")
    String imgBase64;

    @JsonProperty("file_size")
    Long fileSize;
}
