package ai.classifai.database.annotation.properties;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@NonNull
public class AudioProperties {
    Integer frameSize;

    Integer frameRate;

    Integer channels;

    String bits;

    Integer sampleRate;
}
