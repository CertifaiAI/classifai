package ai.classifai.dto;

import ai.classifai.data.enumeration.ProjectInfra;
import ai.classifai.data.enumeration.ProjectType;
import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@NonNull
public class ProjectDTO {
    String projectName;

    String projectId;

    String projectPath;

    Integer projectType;

    List<String> labelList;

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
