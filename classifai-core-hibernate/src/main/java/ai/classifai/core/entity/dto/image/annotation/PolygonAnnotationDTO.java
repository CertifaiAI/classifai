package ai.classifai.core.entity.dto.image.annotation;

import ai.classifai.core.entity.dto.generic.AnnotationDTO;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * DTO class representing PolygonAnnotation entity
 *
 * @author YinChuangSum
 */
@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
public class PolygonAnnotationDTO extends ImageAnnotationDTO
{
    public static PolygonAnnotationDTO toDTOImpl(AnnotationDTO dto)
    {
        if (dto instanceof PolygonAnnotationDTO)
        {
            return (PolygonAnnotationDTO) dto;
        }

        throw new IllegalArgumentException(String.format("%s is expected to be parsed but got %s", PolygonAnnotationDTO.class, dto.getClass()));
    }
}
