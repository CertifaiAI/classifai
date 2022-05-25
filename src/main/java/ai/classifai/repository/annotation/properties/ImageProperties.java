package ai.classifai.repository.annotation.properties;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@NonNull
public class ImageProperties {
    int fileSize;

    int imgDepth;

    int imgOriW;

    int imgOriH;
}
