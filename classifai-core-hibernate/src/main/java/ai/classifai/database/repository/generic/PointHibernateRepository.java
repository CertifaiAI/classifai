package ai.classifai.database.repository.generic;

import ai.classifai.core.service.generic.PointRepository;
import ai.classifai.database.entity.generic.PointEntity;
import ai.classifai.core.entity.model.generic.Point;
import ai.classifai.core.entity.dto.generic.PointDTO;
import ai.classifai.database.entity.generic.ProjectEntity;
import ai.classifai.database.entity.image.annotation.ImageAnnotationEntity;
import lombok.NonNull;

import javax.persistence.EntityManager;
import java.util.UUID;

public class PointHibernateRepository extends AbstractHibernateRepository<Point, PointDTO, UUID, PointEntity> implements PointRepository
{
    public PointHibernateRepository(EntityManager em) {
        super(em, PointEntity.class);
    }

    @Override
    public Point create(@NonNull PointDTO dto)
    {
        PointEntity entity = new PointEntity();
        ImageAnnotationEntity imageAnnotationEntity = em.getReference(ImageAnnotationEntity.class, dto.getAnnotationId());

        entity.fromDTO(dto);
        imageAnnotationEntity.addPoint(entity);
        em.persist(entity);

        return entity;
    }

    public Point update(@NonNull Point point, @NonNull PointDTO pointDTO)
    {
        point.update(pointDTO);
        return em.merge(point);
    }
}
