package ai.classifai.repository.annotation.properties;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@NonNull
public class AudioProperties {
    int frameSize;

    int frameRate;

    int channels;

    String bits;

    int sampleRate;
}
