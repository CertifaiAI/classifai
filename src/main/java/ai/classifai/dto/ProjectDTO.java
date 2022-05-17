package ai.classifai.dto;

import ai.classifai.data.ProjectInfra;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@NonNull
public class ProjectDTO {
    @JsonProperty("project_id")
    String projectId;

    @JsonProperty("project_name")
    String projectName;

    @JsonProperty("is_new")
    Boolean isNewParam;

    @JsonProperty("is_starred")
    Boolean isStarredParam;

    @JsonProperty("is_loaded")
    Boolean isLoadedParam;

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
}
