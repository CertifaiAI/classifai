package ai.classifai.core.service.image.annotation;

import ai.classifai.core.entity.model.image.annotation.BoundingBoxAnnotation;
import ai.classifai.core.entity.dto.image.annotation.BoundingBoxAnnotationDTO;

public interface BoundingBoxAnnotationRepository extends ImageAnnotationRepository<BoundingBoxAnnotation, BoundingBoxAnnotationDTO>
{
}
