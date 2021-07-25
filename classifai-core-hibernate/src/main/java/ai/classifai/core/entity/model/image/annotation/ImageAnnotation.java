package ai.classifai.core.entity.model.image.annotation;

import ai.classifai.core.entity.dto.image.annotation.ImageAnnotationDTO;
import ai.classifai.core.entity.model.generic.Point;
import ai.classifai.core.entity.model.generic.Annotation;

import java.util.List;

public interface ImageAnnotation extends Annotation
{
    List<Point> getPointList();
}
