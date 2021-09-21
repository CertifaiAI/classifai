package ai.classifai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.util.List;

@Builder
@Data
public class ThumbnailProperties {

    @JsonProperty
    int message = 1;

    @NonNull
    @JsonProperty("uuid")
    String uuidParam;

    @NonNull
    @JsonProperty("project_name")
    String projectNameParam;

    @NonNull
    @JsonProperty("img_path")
    String imgPathParam;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("polygons")
    List<AnnotationPointProperties> segmentationParam;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonProperty("bnd_box")
    List<AnnotationPointProperties> boundingBoxParam;

    @NonNull
    @JsonProperty("img_depth")
    Integer imgDepth;

    @NonNull
    @JsonProperty("img_x")
    Integer imgXParam;

    @NonNull
    @JsonProperty("img_y")
    Integer imgYParam;

    @NonNull
    @JsonProperty("img_w")
    Integer imgWParam;

    @NonNull
    @JsonProperty("img_h")
    Integer imgHParam;

    @NonNull
    @JsonProperty("file_size")
    Integer fileSizeParam;

    @NonNull
    @JsonProperty("img_ori_w")
    Integer imgOriWParam;

    @NonNull
    @JsonProperty("img_ori_h")
    Integer imgOriHParam;

    @NonNull
    @JsonProperty("img_thumbnail")
    String imgThumbnailParam;
}
