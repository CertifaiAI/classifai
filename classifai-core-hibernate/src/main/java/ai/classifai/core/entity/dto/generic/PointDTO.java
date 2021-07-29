package ai.classifai.core.entity.dto.generic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO class representing Point entity
 *
 * @author YinChuangSum
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointDTO {
    @Builder.Default
    UUID id = null;

    Float x;
    Float y;

    @Builder.Default
    Float dist2ImgX = 0f;

    @Builder.Default
    Float dist2ImgY = 0f;
    Integer position;

    Long annotationId;
}
