package ai.classifai.core.dto;

import ai.classifai.core.enumeration.ProjectInfra;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@NonNull
public class ProjectDTO {
    @JsonProperty("project_id")
    @JsonInclude(Include.NON_NULL)
    String projectId;

    @JsonProperty("annotation_type")
    @JsonInclude(Include.NON_NULL)
    Integer annotationType;

    @JsonProperty("label_list")
    @JsonInclude(Include.NON_NULL)
    List<String> labelList;

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
