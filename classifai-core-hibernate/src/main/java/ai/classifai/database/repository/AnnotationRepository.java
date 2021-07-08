package ai.classifai.database.repository;

import ai.classifai.database.model.annotation.Annotation;

import javax.persistence.EntityManager;
import java.util.List;

public class AnnotationRepository extends Repository{
    public AnnotationRepository(EntityManager entityManager) {
        super(entityManager);
    }

    public void removeAnnotationList(List<Annotation> annotationList)
    {
        removeItem(annotationList.toArray());
    }

    public void saveAnnotationList(List<Annotation> annotationList)
    {
        saveItem(annotationList.toArray());
    }

}
