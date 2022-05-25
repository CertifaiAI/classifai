package ai.classifai.repository.annotation.properties;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@NonNull
public class VideoProperties {
    String videoPath;

    int videoLength;

    int extractedFrameIndex;

    String videoDuration;

    int framePerSecond;
}
