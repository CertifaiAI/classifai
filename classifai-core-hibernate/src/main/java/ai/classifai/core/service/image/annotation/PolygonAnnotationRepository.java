package ai.classifai.core.service.image.annotation;

import ai.classifai.core.entity.dto.generic.AnnotationDTO;
import ai.classifai.core.entity.dto.image.annotation.BoundingBoxAnnotationDTO;
import ai.classifai.core.entity.model.generic.Annotation;
import ai.classifai.core.entity.model.image.annotation.BoundingBoxAnnotation;
import ai.classifai.core.entity.model.image.annotation.PolygonAnnotation;
import ai.classifai.core.entity.dto.image.annotation.PolygonAnnotationDTO;
import ai.classifai.core.service.generic.Repository;

/**
 * Repository of PolygonAnnotation entity
 *
 * @author YinChuangSum
 */
public interface PolygonAnnotationRepository extends ImageAnnotationRepository, Repository<Annotation, AnnotationDTO, Long>
{
}
