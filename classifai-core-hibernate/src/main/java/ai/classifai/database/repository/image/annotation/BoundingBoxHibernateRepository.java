package ai.classifai.database.repository.image.annotation;

import ai.classifai.core.entity.dto.image.annotation.BoundingBoxAnnotationDTO;
import ai.classifai.core.entity.model.image.annotation.BoundingBoxAnnotation;
import ai.classifai.core.service.image.annotation.BoundingBoxAnnotationRepository;
import ai.classifai.database.entity.image.annotation.BoundingBoxAnnotationEntity;
import lombok.NonNull;

import javax.persistence.EntityManager;

public class BoundingBoxHibernateRepository extends ImageAnnotationHibernateRepository<BoundingBoxAnnotation, BoundingBoxAnnotationDTO, BoundingBoxAnnotationEntity> implements BoundingBoxAnnotationRepository
{
    public BoundingBoxHibernateRepository(EntityManager em) {
        super(em, BoundingBoxAnnotationEntity.class);
    }

    @Override
    public BoundingBoxAnnotation create(@NonNull BoundingBoxAnnotationDTO boundingBoxAnnotationDTO) {
        return null;
    }

    public BoundingBoxAnnotation update(@NonNull BoundingBoxAnnotation boundingBoxAnnotation, @NonNull BoundingBoxAnnotationDTO boundingBoxAnnotationDTO) {
        return null;
    }
}
