package ai.classifai.core.entity.dto.generic;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

/**
 * DTO class representing Data entity
 *
 * @author YinChuangSum
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class DataDTO
{
    @Builder.Default
    UUID id = null;

    String path;
    String checksum;
    Long fileSize;

    @Builder.Default
    UUID projectId = null;

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof DataDTO)
        {
            DataDTO dto = (DataDTO) obj;
            return path.equals(dto.getPath()) && checksum.equals(dto.getChecksum());
        }

        return false;
    }

    @Override
    public int hashCode()
    {
        return checksum.hashCode() + path.hashCode();
    }
}
