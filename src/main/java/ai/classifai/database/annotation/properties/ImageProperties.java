package ai.classifai.database.annotation.properties;

import lombok.Data;

@Data
public class ImageProperties {
    String projectId;

    Integer fileSize;

    Integer imgDepth;

    Integer imgOriW;

    Integer imgOriH;
}
