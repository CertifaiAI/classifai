package ai.classifai.core.entity.model.generic;

import ai.classifai.core.entity.dto.generic.AnnotationDTO;
import ai.classifai.core.entity.dto.generic.DataDTO;
import ai.classifai.core.entity.dto.generic.DataVersionDTO;
import ai.classifai.core.entity.trait.HasDTO;
import ai.classifai.core.entity.trait.HasId;

import java.util.List;
import java.util.UUID;

public interface DataVersion extends HasDTO<DataVersionDTO>, HasId<DataVersion.DataVersionId>
{
    Data getData();

    Version getVersion();

    List<Annotation> getAnnotations();

    interface DataVersionId
    {
        UUID getDataId();
        UUID getVersionId();
    }
}
