package ai.classifai.core.service.generic;

import ai.classifai.core.entity.model.generic.Annotation;
import ai.classifai.core.entity.dto.generic.AnnotationDTO;
import ai.classifai.core.entity.model.generic.Label;
import ai.classifai.core.service.generic.Repository;

/**
 * Abstract repository of Annotation entity
 *
 * @author YinChuangSum
 */
public interface AnnotationRepository extends AbstractRepository<Annotation, AnnotationDTO, Long>
{
    Annotation setLabel(Annotation annotation, Label label);
}
