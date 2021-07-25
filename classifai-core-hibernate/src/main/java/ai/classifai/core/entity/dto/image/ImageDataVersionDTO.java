package ai.classifai.core.entity.dto.image;

import ai.classifai.core.entity.dto.generic.DataVersionDTO;
import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ImageDataVersionDTO extends DataVersionDTO
{
    @Builder.Default
    Float imgX = 0f;

    @Builder.Default
    Float imgY = 0f;

    @Builder.Default
    Float imgW = 0f;

    @Builder.Default
    Float imgH = 0f;

    public static ImageDataVersionDTO toDTOImpl(DataVersionDTO dto)
    {
        if (dto instanceof ImageDataVersionDTO)
        {
            return (ImageDataVersionDTO) dto;
        }

        throw new IllegalArgumentException(String.format("%s is expected to be parsed but got %s", ImageDataVersionDTO.class, dto.getClass()));
    }
}
