package ai.classifai.core.entities;

import ai.classifai.core.entities.dto.dataversion.DataVersionDTO;
import ai.classifai.core.entities.traits.HasDTO;
import ai.classifai.core.entities.traits.HasId;

import java.util.List;
import java.util.UUID;

public interface DataVersion extends HasId<DataVersion.DataVersionId>,HasDTO<DataVersionDTO>
{
    List<Annotation> getAnnotationList();

    interface DataVersionId
    {
        UUID getDataId();
        UUID getVersionId();
    }
}
