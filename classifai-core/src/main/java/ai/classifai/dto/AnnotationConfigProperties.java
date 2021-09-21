package ai.classifai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

import java.util.List;

@NonNull
@AllArgsConstructor
@Data
public class AnnotationConfigProperties {
    @JsonProperty
    String checksum;

    @JsonProperty("img_path")
    String imgPath;

    @JsonProperty("version_list")
    List<VersionConfigProperties> versionList;

    @JsonProperty("img_depth")
    Integer imgDepth;

    @JsonProperty("img_ori_w")
    Integer imgOriW;

    @JsonProperty("img_ori_h")
    Integer imgOriH;

    @JsonProperty("file_size")
    Integer fileSize;
}
