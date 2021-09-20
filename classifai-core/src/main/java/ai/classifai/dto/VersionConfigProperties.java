package ai.classifai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;

@NoArgsConstructor
@Data
public class VersionConfigProperties {
    @JsonProperty("version_uuid")
    String versionUuid;

    @JsonProperty("annotation_data")
    HashMap<String, List<AnnotationPointProperties>> annotationData;
}
