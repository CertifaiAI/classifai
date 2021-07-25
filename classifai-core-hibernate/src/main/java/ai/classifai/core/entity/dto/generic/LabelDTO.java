package ai.classifai.core.entity.dto.generic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabelDTO {
    @Builder.Default
    UUID id = null;

    String name;

    @Builder.Default
    String color = "rgba(255,255,0,0.8)";

    @Builder.Default
    UUID versionId = null;
}
