package ai.classifai.database.repository.image.annotation;

import ai.classifai.core.entity.dto.image.annotation.PolygonAnnotationDTO;
import ai.classifai.core.entity.model.image.annotation.PolygonAnnotation;
import ai.classifai.core.service.image.annotation.PolygonAnnotationRepository;
import ai.classifai.database.entity.image.annotation.PolygonAnnotationEntity;
import lombok.NonNull;

import javax.persistence.EntityManager;

public class PolygonHibernateRepository extends ImageAnnotationHibernateRepository<PolygonAnnotation,
        PolygonAnnotationDTO, PolygonAnnotationEntity> implements PolygonAnnotationRepository
{
    public PolygonHibernateRepository(EntityManager em)
    {
        super(em, PolygonAnnotationEntity.class);
    }

    @Override
    public PolygonAnnotation create(@NonNull PolygonAnnotationDTO polygonAnnotationDTO) {
        return null;
    }

    public PolygonAnnotation update(@NonNull PolygonAnnotation polygonAnnotation, @NonNull PolygonAnnotationDTO polygonAnnotationDTO) {
        return null;
    }
}
