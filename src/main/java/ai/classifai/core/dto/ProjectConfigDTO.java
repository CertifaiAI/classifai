package ai.classifai.core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@NonNull
public class ProjectConfigDTO extends ProjectInfoDTO {
    @JsonProperty("tool")
    String toolName;

    @JsonProperty("tool_version")
    String toolVersion;

    @JsonProperty("updated_date")
    String updatedDate;

    @JsonProperty("annotation_type")
    String annotationType;

    @JsonProperty("project_path")
    String projectPath;

    @JsonProperty("current_version")
    String currentVersion;

    @JsonProperty("project_version")
    String projectVersion;

    @JsonProperty("uuid_version_list")
    String uuidVersionList;

    @JsonProperty("label_version_list")
    String labelVersionList;

}
