package ai.classifai.db.repository;

import ai.classifai.db.entities.annotation.AnnotationEntity;

import javax.persistence.EntityManager;
import java.util.List;

public class AnnotationRepository extends Repository{
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
