package ai.classifai.database.repository.image.annotation;

import ai.classifai.core.entity.dto.generic.AnnotationDTO;
import ai.classifai.core.entity.dto.image.annotation.BoundingBoxAnnotationDTO;
import ai.classifai.core.entity.model.generic.Annotation;
import ai.classifai.core.entity.model.image.annotation.BoundingBoxAnnotation;
import ai.classifai.core.service.image.annotation.BoundingBoxAnnotationRepository;
import ai.classifai.database.entity.generic.DataVersionEntity;
import ai.classifai.database.entity.generic.DataVersionKey;
import ai.classifai.database.entity.generic.LabelEntity;
import ai.classifai.database.entity.image.annotation.BoundingBoxAnnotationEntity;
import lombok.NonNull;

import javax.persistence.EntityManager;

public class BoundingBoxHibernateRepository extends ImageAnnotationHibernateRepository implements BoundingBoxAnnotationRepository
{
    public BoundingBoxHibernateRepository(EntityManager em) {
        super(em, BoundingBoxAnnotationEntity.class);
    }

    @Override
    public Annotation create(@NonNull AnnotationDTO annotationDTO)
    {
        BoundingBoxAnnotationDTO dto = BoundingBoxAnnotationDTO.toDTOImpl(annotationDTO);
        BoundingBoxAnnotationEntity entity = new BoundingBoxAnnotationEntity();
        entity.fromDTO(dto);

        DataVersionEntity dataVersionEntity = em.getReference(DataVersionEntity.class, new DataVersionKey(dto.getDataId(), dto.getVersionId()));
        LabelEntity labelEntity = em.getReference(LabelEntity.class, dto.getLabelId());

        dataVersionEntity.addAnnotation(entity);
        entity.setLabel(labelEntity);

        em.persist(entity);
        return entity;
    }
}
