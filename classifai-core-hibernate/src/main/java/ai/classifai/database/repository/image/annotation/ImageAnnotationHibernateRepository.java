package ai.classifai.database.repository.image.annotation;

import ai.classifai.core.entity.dto.image.annotation.ImageAnnotationDTO;
import ai.classifai.core.entity.model.image.annotation.ImageAnnotation;
import ai.classifai.core.service.image.annotation.ImageAnnotationRepository;
import ai.classifai.database.entity.generic.AnnotationEntity;
import ai.classifai.database.entity.image.annotation.ImageAnnotationEntity;
import ai.classifai.database.repository.generic.AnnotationHibernateRepository;

import javax.persistence.EntityManager;

public class ImageAnnotationHibernateRepository extends AnnotationHibernateRepository implements ImageAnnotationRepository
{
    public ImageAnnotationHibernateRepository(EntityManager em, Class<? extends ImageAnnotationEntity> entityClass) {
        super(em, entityClass);
    }
}
