package ai.classifai.database.repository.generic;

import ai.classifai.core.entity.dto.generic.AnnotationDTO;
import ai.classifai.core.entity.model.generic.Data;
import ai.classifai.core.entity.model.generic.Label;
import ai.classifai.core.service.generic.AnnotationRepository;
import ai.classifai.core.entity.model.generic.Annotation;
import ai.classifai.database.entity.generic.AnnotationEntity;
import ai.classifai.database.entity.generic.DataVersionEntity;
import ai.classifai.database.entity.generic.LabelEntity;
import ai.classifai.database.repository.generic.AbstractHibernateRepository;
import lombok.NonNull;

import javax.persistence.EntityManager;

public class AnnotationHibernateRepository extends AbstractHibernateRepository<Annotation, AnnotationDTO, Long, AnnotationEntity> implements AnnotationRepository
{
    public AnnotationHibernateRepository(EntityManager em)
    {
        super(em, AnnotationEntity.class);
    }

    public AnnotationHibernateRepository(EntityManager em, Class<? extends AnnotationEntity> entityClass)
    {
        super(em, entityClass);
    }

    @Override
    public Annotation setLabel(Annotation annotation, Label label)
    {
        AnnotationEntity entity = toEntityImpl(annotation);
        LabelEntity labelEntity = em.getReference(LabelEntity.class, label.getId());
        entity.setLabel(labelEntity);
        return em.merge(entity);
    }

    @Override
    public void delete(@NonNull Annotation annotation)
    {
        AnnotationEntity entity = toEntityImpl(annotation);
        DataVersionEntity dataVersionEntity = em.getReference(DataVersionEntity.class, annotation.getDataVersion().getId());
        dataVersionEntity.removeAnnotation(entity);
        super.delete(entity);
    }
}
