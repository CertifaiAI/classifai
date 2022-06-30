package ai.classifai.core.properties.audio;

import java.util.List;

public class AudioDataProperties {
    Integer frameSize;

    Integer channel;

    Float frameRate;

    Integer bits;

    Float sampleRate;

    String encoding;

    List<AudioRegionsProperties> audioRegionsPropertiesList;
}
