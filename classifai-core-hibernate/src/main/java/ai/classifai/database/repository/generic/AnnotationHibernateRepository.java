package ai.classifai.database.repository.generic;

import ai.classifai.core.entity.dto.generic.AnnotationDTO;
import ai.classifai.core.service.generic.AnnotationRepository;
import ai.classifai.core.entity.model.generic.Annotation;
import ai.classifai.database.repository.generic.AbstractHibernateRepository;

import javax.persistence.EntityManager;

public abstract class AnnotationHibernateRepository<Entity extends Annotation, DTO extends AnnotationDTO, EntityImpl extends Entity> extends AbstractHibernateRepository<Entity, DTO, Long, EntityImpl> implements AnnotationRepository<Entity, DTO>
{
    public AnnotationHibernateRepository(EntityManager em, Class<EntityImpl> entityClass) {
        super(em, entityClass);
    }
}
