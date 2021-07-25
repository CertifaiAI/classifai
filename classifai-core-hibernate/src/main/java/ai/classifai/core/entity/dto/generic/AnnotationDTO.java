package ai.classifai.core.entity.dto.generic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class AnnotationDTO
{
    Long id;
    Integer position;
    UUID labelId;
    UUID dataId;
    UUID versionId;
}
