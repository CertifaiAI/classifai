package ai.classifai.core.dto;

import ai.classifai.core.properties.AudioRegionsProperties;
import io.vertx.sqlclient.Tuple;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AudioDTO {
    String projectName;

    Float audioDuration;

    Integer frameSize;

    Float frameRate;

    Integer channel;

    Integer sampleRate;

    Integer sampleSizeInBit;

    List<AudioRegionsProperties> audioRegionsPropertiesList;

    public Tuple getTuple() {
        return Tuple.of(
                projectName,
                audioDuration,
                frameSize,
                frameRate,
                channel,
                sampleRate,
                sampleSizeInBit,
                audioRegionsPropertiesList
        );
    }
}
