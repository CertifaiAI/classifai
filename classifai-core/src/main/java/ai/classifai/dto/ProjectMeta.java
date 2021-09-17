package ai.classifai.dto;

import ai.classifai.util.ParamConfig;
import ai.classifai.util.project.ProjectInfra;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class ProjectMeta {
    @JsonProperty("project_name")
    String projectName;

    @JsonProperty("project_path")
    String projectPath;

    @JsonProperty("is_new")
    Boolean isNewParam;

    @JsonProperty("is_starred")
    Boolean isStarredParam;

    @JsonProperty("is_loaded")
    Boolean isLoadedParam;

    @JsonProperty("is_cloud")
    Boolean isCloud;

    @JsonProperty("project_infra")
    ProjectInfra projectInfraParam;

    @JsonProperty("created_date")
    String createdDateParam;

    @JsonProperty("last_modified_date")
    String lastModifiedDate;

    @JsonProperty("current_version")
    String currentVersionParam;

    @JsonProperty("total_uuid")
    int totalUuidParam;

    @JsonProperty("root_path_valid")
    Boolean isRootPathValidParam;
}
