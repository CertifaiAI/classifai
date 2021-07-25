package ai.classifai.core.entity.dto.image;

import ai.classifai.core.entity.dto.generic.DataDTO;
import io.vertx.core.json.JsonObject;
import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ImageDataDTO extends DataDTO
{
    Integer depth;
    Integer height;
    Integer width;

    public static ImageDataDTO toDTOImpl(DataDTO dto)
    {
        if (dto instanceof ImageDataDTO)
        {
            return (ImageDataDTO) dto;
        }

        throw new IllegalArgumentException(String.format("%s is expected to be parsed but got %s", ImageDataDTO.class, dto.getClass()));
    }
}
