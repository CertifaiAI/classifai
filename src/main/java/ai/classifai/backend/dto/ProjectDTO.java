package ai.classifai.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@NonNull
public class ProjectDTO {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String projectName;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    String projectId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    String projectPath;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    Integer projectType;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    List<String> labelList;

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
