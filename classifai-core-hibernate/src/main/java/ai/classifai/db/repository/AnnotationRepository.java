package ai.classifai.db.repository;

import ai.classifai.core.entities.traits.HasId;
import ai.classifai.db.entities.annotation.AnnotationEntity;

import javax.persistence.EntityManager;
import java.util.List;

public class AnnotationRepository extends Repository<HasId<R>, S, R> {
    public AnnotationRepository(EntityManager entityManager) {
        super(entityManager);
    }

    public void removeAnnotationList(List<AnnotationEntity> annotationEntityList)
    {
        removeItem(annotationEntityList.toArray());
    }

    public void saveAnnotationList(List<AnnotationEntity> annotationEntityList)
    {
        saveItem(annotationEntityList.toArray());
    }

}
