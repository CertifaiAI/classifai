package ai.classifai.database.repository.image.annotation;

import ai.classifai.core.entity.dto.image.annotation.ImageAnnotationDTO;
import ai.classifai.core.entity.model.image.annotation.ImageAnnotation;
import ai.classifai.core.service.image.annotation.ImageAnnotationRepository;
import ai.classifai.database.repository.generic.AnnotationHibernateRepository;

import javax.persistence.EntityManager;

public abstract class ImageAnnotationHibernateRepository<Entity extends ImageAnnotation, DTO extends ImageAnnotationDTO,
        EntityImpl extends Entity> extends AnnotationHibernateRepository<Entity, DTO, EntityImpl>
        implements ImageAnnotationRepository<Entity, DTO>
{
    public ImageAnnotationHibernateRepository(EntityManager em, Class<EntityImpl> entityClass) {
        super(em, entityClass);
    }
}
