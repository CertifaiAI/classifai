package ai.classifai.dto;

import ai.classifai.dto.properties.ImageDataProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@NonNull
public class ProjectConfigDTO extends ProjectDTO {
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

    // will see if this is needed
//    @JsonProperty("label_version_list")
//    String labelVersionList;

    // will add later
    @JsonProperty
    Map<String, ImageDataProperties> content;
}
