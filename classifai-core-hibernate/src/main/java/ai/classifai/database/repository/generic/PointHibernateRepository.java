package ai.classifai.database.repository.generic;

import ai.classifai.core.service.generic.PointRepository;
import ai.classifai.database.entity.generic.PointEntity;
import ai.classifai.core.entity.model.generic.Point;
import ai.classifai.core.entity.dto.generic.PointDTO;
import lombok.NonNull;

import javax.persistence.EntityManager;
import java.util.UUID;

public class PointHibernateRepository extends AbstractHibernateRepository<Point, PointDTO, UUID, PointEntity> implements PointRepository
{
    public PointHibernateRepository(EntityManager em) {
        super(em, PointEntity.class);
    }

    @Override
    public Point create(@NonNull PointDTO pointDTO) {
        return null;
    }

    public Point update(@NonNull Point point, @NonNull PointDTO pointDTO) {
        return null;
    }
}
