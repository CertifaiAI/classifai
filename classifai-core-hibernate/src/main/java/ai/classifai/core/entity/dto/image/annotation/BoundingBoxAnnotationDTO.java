package ai.classifai.core.entity.dto.image.annotation;

import ai.classifai.core.entity.dto.generic.AnnotationDTO;
import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
public class BoundingBoxAnnotationDTO extends ImageAnnotationDTO
{
    public static BoundingBoxAnnotationDTO toDTOImpl(AnnotationDTO dto)
    {
        if (dto instanceof BoundingBoxAnnotationDTO)
        {
            return (BoundingBoxAnnotationDTO) dto;
        }

        throw new IllegalArgumentException(String.format("%s is expected to be parsed but got %s", BoundingBoxAnnotationDTO.class, dto.getClass()));
    }
}
