package ai.classifai.core.entities;

import ai.classifai.core.entities.dto.annotation.AnnotationDTO;
import ai.classifai.core.entities.traits.HasDTO;
import ai.classifai.core.entities.traits.HasId;
import ai.classifai.core.entities.traits.HasOrder;

import java.util.List;

public interface Annotation extends HasId<Long>, HasDTO<AnnotationDTO>, HasOrder
{
    Label getLabel();
}
