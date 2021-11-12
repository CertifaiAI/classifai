package ai.classifai.core.entities.properties;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@NonNull
public class ProjectConfigProperties {
    @JsonProperty("tool")
    String toolName;

    @JsonProperty("tool_version")
    String toolVersion;

    @JsonProperty("updated_date")
    String updateDate;

    @JsonProperty("project_id")
    String projectID;

    @JsonProperty("project_name")
    String projectName;

    @JsonProperty("annotation_type")
    String annotationType;

    @JsonProperty("project_path")
    String projectPath;

    @JsonProperty("is_new")
    Boolean isNew;

    @JsonProperty("is_starred")
    Boolean isStarred;

    @JsonProperty("project_infra")
    String projectInfra;

    @JsonProperty("current_version")
    String currentVersion;

    @JsonProperty("project_version")
    String projectVersion;

    @JsonProperty("uuid_version_list")
    String uuidVersionList;

    @JsonProperty("label_version_list")
    String labelVersionList;

    @JsonProperty
    Map<String, ImageDataProperties> content;
}
