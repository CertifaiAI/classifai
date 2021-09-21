package ai.classifai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@NonNull
@Builder
@Data
public class VersionConfigProperties {
    @JsonProperty("version_uuid")
    String versionUuid;

    @JsonProperty("annotation_data")
    DataInfoProperties annotationData;
}
