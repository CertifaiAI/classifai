package ai.classifai.database.repository;

import ai.classifai.database.model.dataVersion.DataVersion;

import javax.persistence.EntityManager;
import java.util.List;

public class DataVersionRepository extends BaseRepository
{
    public DataVersionRepository(EntityManager entityManager)
    {
        super(entityManager);
    }

    public void saveDataVersion(DataVersion dataVersion)
    {
        saveItem(() -> addDataVersion(dataVersion));
    }

    public void saveDataVersionList(List<DataVersion> dataVersionList)
    {
       saveItem(() -> dataVersionList.forEach(this::addDataVersion));
    }

    private void addDataVersion(DataVersion dataVersion)
    {
        if (dataVersion.getDataVersionKey() == null)
        {
            entityManager.persist(dataVersion);
        }
        else
        {
            entityManager.merge(dataVersion);
        }
    }

}
