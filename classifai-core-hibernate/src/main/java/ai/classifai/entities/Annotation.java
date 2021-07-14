package ai.classifai.entities;

import ai.classifai.entities.dto.annotation.AnnotationDTO;
import ai.classifai.entities.traits.HasDTO;
import ai.classifai.entities.traits.HasId;

public interface Annotation extends HasId<Long>, HasDTO<AnnotationDTO>
{
    Label getLabel();
    Integer getPosition();
}
