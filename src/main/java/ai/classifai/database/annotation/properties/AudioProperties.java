package ai.classifai.database.annotation.properties;

import lombok.Data;

@Data
public class AudioProperties {
    String audioProperties;

    Integer frameSize;

    Integer frameRate;

    Integer channels;

    String bits;

    Integer sampleRate;
}
