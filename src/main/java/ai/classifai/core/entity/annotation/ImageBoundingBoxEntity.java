package ai.classifai.core.entity.annotation;

import ai.classifai.core.dto.properties.BoundingBoxProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.sqlclient.Tuple;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ImageBoundingBoxEntity {
    @JsonProperty
    String projectId;

    @JsonProperty
    String projectName;

    @JsonProperty
    String imgUuid;

    @JsonProperty
    String imgPath;

    @JsonProperty
    Integer imgDepth;

    @JsonProperty("img_ori_w")
    Integer imgOriginalWidth;

    @JsonProperty("img_ori_h")
    Integer imgOriginalHeight;

    @JsonProperty("img_thumbnail")
    String imgBase64;

    @JsonProperty
    Long fileSize;

    @JsonProperty("img_x")
    @Builder.Default Integer imgX = 0;

    @JsonProperty("img_y")
    @Builder.Default Integer imgY = 0;

    @JsonProperty("img_w")
    @Builder.Default Integer imgW = 0;

    @JsonProperty("img_h")
    @Builder.Default Integer imgH = 0;

    @JsonProperty
    @Builder.Default List<BoundingBoxProperties> boundingBoxPropertiesList = new ArrayList<>();

    public Tuple getTuple() {
        return Tuple.of(
                imgUuid,
                projectId,
                projectName,
                imgPath,
                imgDepth,
                imgOriginalWidth,
                imgOriginalHeight,
                imgX,
                imgY,
                imgW,
                imgH,
                boundingBoxPropertiesList,
                fileSize,
                imgBase64
        );
    }

}
