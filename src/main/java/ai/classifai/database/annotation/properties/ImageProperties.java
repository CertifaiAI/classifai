package ai.classifai.database.annotation.properties;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@NonNull
public class ImageProperties {
    Integer fileSize;

    Integer imgDepth;

    Integer imgOriW;

    Integer imgOriH;
}
