package ai.classifai.entities;

import ai.classifai.entities.dto.DataDTO;
import ai.classifai.entities.traits.HasDTO;
import ai.classifai.entities.traits.HasId;

import java.util.UUID;

public interface Data extends HasId<UUID>, HasDTO<DataDTO>
{
    String getPath();
    String getChecksum();
    Long getFileSize();
}
