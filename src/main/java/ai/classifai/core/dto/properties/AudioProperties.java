package ai.classifai.core.dto.properties;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AudioProperties {
    String projectName;

    String projectPath;
}
