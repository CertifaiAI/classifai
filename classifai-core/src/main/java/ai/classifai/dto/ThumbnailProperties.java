package ai.classifai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class ThumbnailProperties {

    @JsonProperty("uuid")
    String uuidParam;

    @JsonProperty("project_name")
    String projectNameParam;

    @JsonProperty("img_path")
    String imgPathParam;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("polygons")
    List<AnnotationPointProperties> segmentationParam;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("bnd_box")
    List<AnnotationPointProperties> boundingBoxParam;

    @JsonProperty("img_depth")
    Integer imgDepth;

    @JsonProperty("img_x")
    Integer imgXParam;

    @JsonProperty("img_y")
    Integer imgYParam;

    @JsonProperty("img_w")
    Integer imgWParam;

    @JsonProperty("img_h")
    Integer imgHParam;

    @JsonProperty("file_size")
    Integer fileSizeParam;

    @JsonProperty("img_ori_w")
    Integer imgOriWParam;

    @JsonProperty("img_ori_h")
    Integer imgOriHParam;

    @JsonProperty("img_thumbnail")
    String imgThumbnailParam;
}
