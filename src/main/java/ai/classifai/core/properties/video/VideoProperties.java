package ai.classifai.core.properties.video;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class VideoProperties {
    String projectId;

    String projectName;

    String projectPath;

    List<VideoDataProperties> videoDataPropertiesList;
}
