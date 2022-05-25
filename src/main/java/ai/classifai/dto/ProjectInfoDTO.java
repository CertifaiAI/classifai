package ai.classifai.dto;

import ai.classifai.data.enumeration.ProjectInfra;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@NonNull
public class ProjectInfoDTO {
    @JsonProperty("project_id")
    String projectId;

    @JsonProperty("project_name")
    String projectName;

    @JsonProperty("is_new")
    boolean isNewParam;

    @JsonProperty("is_starred")
    boolean isStarredParam;

    @JsonProperty("is_loaded")
    boolean isLoadedParam;

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
