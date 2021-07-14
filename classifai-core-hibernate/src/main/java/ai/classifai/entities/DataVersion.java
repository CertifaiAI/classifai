package ai.classifai.entities;

import ai.classifai.entities.dto.dataversion.DataVersionDTO;
import ai.classifai.entities.traits.HasDTO;

import java.util.List;

public interface DataVersion extends HasDTO<DataVersionDTO>
{
    Data getData();
    Version getVersion();
    List<Annotation> getAnnotationList();
}
