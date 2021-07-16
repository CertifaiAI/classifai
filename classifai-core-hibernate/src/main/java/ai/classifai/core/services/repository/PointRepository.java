package ai.classifai.core.services.repository;

import ai.classifai.core.entities.Point;
import ai.classifai.core.entities.dto.PointDTO;

import java.util.UUID;

public interface PointRepository extends Repository<Point, PointDTO, UUID>
{
}
