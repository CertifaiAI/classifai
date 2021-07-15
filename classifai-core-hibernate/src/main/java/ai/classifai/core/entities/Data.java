package ai.classifai.core.entities;

import ai.classifai.core.entities.dto.DataDTO;
import ai.classifai.core.entities.traits.HasDTO;
import ai.classifai.core.entities.traits.HasId;

import java.util.UUID;

public interface Data extends HasId<UUID>, HasDTO<DataDTO>
{
    String getPath();
    String getChecksum();
    Long getFileSize();
}
