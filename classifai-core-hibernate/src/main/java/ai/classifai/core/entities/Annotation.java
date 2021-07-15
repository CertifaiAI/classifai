package ai.classifai.core.entities;

import ai.classifai.core.entities.dto.annotation.AnnotationDTO;
import ai.classifai.core.entities.traits.HasDTO;
import ai.classifai.core.entities.traits.HasId;

public interface Annotation extends HasId<Long>, HasDTO<AnnotationDTO>
{
    Label getLabel();
    Integer getPosition();
}
