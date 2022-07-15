package ai.classifai.core.dto;

import ai.classifai.core.properties.audio.AudioRegionsProperties;
import io.vertx.sqlclient.Tuple;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AudioDTO {
    String uuid;

    String projectId;

    String audioPath;

    Float audioDuration;

    Integer frameSize;

    Float frameRate;

    Integer channel;

    Float sampleRate;

    Integer sampleSizeInBit;

    List<AudioRegionsProperties> audioRegionsPropertiesList;

    public Tuple getTuple() {
        return Tuple.of(
                uuid,
                projectId,
                audioPath,
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
