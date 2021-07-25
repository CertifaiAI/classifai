package ai.classifai.core.entity.dto.image.annotation;

import ai.classifai.core.entity.dto.generic.AnnotationDTO;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
public abstract class ImageAnnotationDTO extends AnnotationDTO
{
    @Builder.Default
    List<UUID> pointIdList = new ArrayList<>();
}
