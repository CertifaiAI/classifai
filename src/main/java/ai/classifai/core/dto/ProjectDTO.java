package ai.classifai.core.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@NonNull
public class ProjectDTO {
    @JsonProperty("project_name")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String projectName;

    @JsonProperty("project_id")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String projectId;

    @JsonProperty("project_path")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String projectPath;

    @JsonProperty("annotation_type")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Integer projectType;

    @JsonProperty("label_list")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    List<String> labelList;

    @JsonProperty("project_infra")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Integer projectInfra;

    // extra video parameters
//    String videoFilePath;
//
//    String videoDuration;
//
//    int videoLength;
//
//    int extractedFrameIndex;
//
//    int framePerSecond;

    // extra tabular parameters
//    String tabularFilePath;

    // extra audio parameters
//    String audioFilePath;
}
