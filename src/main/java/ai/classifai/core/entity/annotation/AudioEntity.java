package ai.classifai.core.entity.annotation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AudioEntity {
    @JsonProperty("project_name")
    String projectName;

    @JsonProperty("uuid")
    String uuid;

    @JsonProperty("file_size")
    int fileSize;

}
