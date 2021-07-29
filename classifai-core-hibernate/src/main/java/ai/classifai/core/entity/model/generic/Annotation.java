package ai.classifai.core.entity.model.generic;

import ai.classifai.core.entity.dto.generic.AnnotationDTO;
import ai.classifai.core.entity.trait.HasDTO;
import ai.classifai.core.entity.trait.HasId;

/**
 * Annotation entity interface
 *
 * @author YinChuangSum
 */
public interface Annotation extends HasDTO<AnnotationDTO>, HasId<Long>
{
    Integer getPosition();

    Label getLabel();

    DataVersion getDataVersion();
}
