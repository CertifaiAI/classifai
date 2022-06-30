package ai.classifai.core.entity.annotation;

import ai.classifai.core.dto.AudioDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AudioEntity implements AnnotationEntity<AudioDTO> {

    String projectName;

    String uuid;

    Long fileSize;

    @Override
    public AudioDTO toDto() {
        return null;
    }
}
