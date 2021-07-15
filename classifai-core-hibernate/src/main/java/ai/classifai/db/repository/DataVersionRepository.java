package ai.classifai.db.repository;

import ai.classifai.db.entities.dataVersion.DataVersionEntity;

import javax.persistence.EntityManager;

public class DataVersionRepository extends Repository
{
    public DataVersionRepository(EntityManager entityManager) {
        super(entityManager);
    }

    public void saveDataVersion(DataVersionEntity dataVersionEntity)
    {
        // save annotation
        saveItem(dataVersionEntity.getAnnotationEntities().toArray());

        // save dataversion
        saveItem(dataVersionEntity);
    }
}
