package ai.classifai.database.annotation.properties;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@NonNull
public class VideoProperties {
    String videoPath;

    Integer videoLength;

    Integer extractedFrameIndex;

    String videoDuration;

    Integer framePerSecond;
}
