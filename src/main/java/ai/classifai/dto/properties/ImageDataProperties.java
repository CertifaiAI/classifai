package ai.classifai.dto.properties;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@NonNull
public class ImageDataProperties {
    @JsonProperty
    String checksum;

    @JsonProperty("img_path")
    String imgPath;

    // will be added later
//    @JsonProperty("version_list")
//    List<VersionConfigProperties> versionList;

    @JsonProperty("img_depth")
    Integer imgDepth;

    @JsonProperty("img_ori_w")
    Integer imgOriW;

    @JsonProperty("img_ori_h")
    Integer imgOriH;

    @JsonProperty("file_size")
    Integer fileSize;
}
