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
public class PointDTO {
    @Builder.Default
    UUID id = null;

    Float x;
    Float y;
    Float dist2ImgX;
    Float dist2ImgY;
    Integer position;
}
