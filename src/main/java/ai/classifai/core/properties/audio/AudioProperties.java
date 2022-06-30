package ai.classifai.core.properties.audio;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AudioProperties {
    String projectId;

    String projectName;

    String projectPath;

    List<AudioDataProperties> audioDataPropertiesList;

}
