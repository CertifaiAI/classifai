package ai.classifai.core.service.generic;

import ai.classifai.core.entity.model.generic.Point;
import ai.classifai.core.entity.dto.generic.PointDTO;

import java.util.UUID;

public interface PointRepository extends Repository<Point, PointDTO, UUID>
{
    Point update(Point point, PointDTO dto);
}