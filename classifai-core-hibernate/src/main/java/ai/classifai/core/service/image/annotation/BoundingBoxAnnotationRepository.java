package ai.classifai.core.service.image.annotation;

import ai.classifai.core.entity.dto.generic.AnnotationDTO;
import ai.classifai.core.entity.model.generic.Annotation;
import ai.classifai.core.entity.model.image.annotation.BoundingBoxAnnotation;
import ai.classifai.core.entity.dto.image.annotation.BoundingBoxAnnotationDTO;
import ai.classifai.core.service.generic.Repository;

import java.util.UUID;

/**
 * Repository of BoundingBoxAnnotation entity
 *
 * @author YinChuangSum
 */
public interface BoundingBoxAnnotationRepository extends ImageAnnotationRepository, Repository<Annotation, AnnotationDTO, Long>
{
}
