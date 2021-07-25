package ai.classifai.core.entity.model.generic;

import ai.classifai.core.entity.dto.generic.DataDTO;
import ai.classifai.core.entity.trait.HasDTO;
import ai.classifai.core.entity.trait.HasId;

import java.util.UUID;

public interface Data extends HasDTO<DataDTO>, HasId<UUID>
{
    String getPath();

    String getChecksum();

    Long getFileSize();

    Project getProject();
}
