package ai.classifai.database.repository.image.annotation;

import ai.classifai.core.entity.dto.generic.AnnotationDTO;
import ai.classifai.core.entity.dto.image.annotation.BoundingBoxAnnotationDTO;
import ai.classifai.core.entity.dto.image.annotation.PolygonAnnotationDTO;
import ai.classifai.core.entity.model.generic.Annotation;
import ai.classifai.core.entity.model.image.annotation.PolygonAnnotation;
import ai.classifai.core.service.image.annotation.PolygonAnnotationRepository;
import ai.classifai.database.entity.image.annotation.BoundingBoxAnnotationEntity;
import ai.classifai.database.entity.image.annotation.ImageAnnotationEntity;
import ai.classifai.database.entity.image.annotation.PolygonAnnotationEntity;
import lombok.NonNull;

import javax.persistence.EntityManager;
import java.util.List;

public class PolygonHibernateRepository extends ImageAnnotationHibernateRepository implements PolygonAnnotationRepository
{

    public PolygonHibernateRepository(EntityManager em) {
        super(em, PolygonAnnotationEntity.class);
    }

    @Override
    public Annotation create(@NonNull AnnotationDTO annotationDTO)
    {
        PolygonAnnotationDTO dto = PolygonAnnotationDTO.toDTOImpl(annotationDTO);
        PolygonAnnotationEntity entity = new PolygonAnnotationEntity();
        entity.fromDTO(dto);

        em.persist(entity);
        return entity;
    }
}
