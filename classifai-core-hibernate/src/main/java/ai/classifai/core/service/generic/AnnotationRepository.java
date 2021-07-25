package ai.classifai.core.service.generic;

import ai.classifai.core.entity.model.generic.Annotation;
import ai.classifai.core.entity.dto.generic.AnnotationDTO;
import ai.classifai.core.service.generic.Repository;

public interface AnnotationRepository<Entity extends Annotation, DTO extends AnnotationDTO> extends Repository<Entity, DTO, Long>
{
}
