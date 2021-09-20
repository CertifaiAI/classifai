package ai.classifai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;

@NoArgsConstructor
@Data
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
    HashMap<String, AnnotationConfigProperties> content;
}
