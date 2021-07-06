package ai.classifai.database.repository;

import ai.classifai.database.model.dataVersion.DataVersion;

import javax.persistence.EntityManager;

public class DataVersionRepository extends Repository
{
    public DataVersionRepository(EntityManager entityManager) {
        super(entityManager);
    }

    public void saveDataVersion(DataVersion dataVersion)
    {
        // save annotation
        saveItem(dataVersion.getAnnotations().toArray());

        // save dataversion
        saveItem(dataVersion);
    }
}
