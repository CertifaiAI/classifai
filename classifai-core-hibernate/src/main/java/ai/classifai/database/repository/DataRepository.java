package ai.classifai.database.repository;

import ai.classifai.database.model.data.Data;

import javax.persistence.EntityManager;
import java.util.List;

public class DataRepository extends BaseRepository
{
    public DataRepository(EntityManager entityManager) {
        super(entityManager);
    }

    public void saveBatchData(List<Data> dataList)
    {
        saveItem(() -> dataList.forEach(this::saveData));
    }

    public void addSingleData(Data data)
    {
        saveItem(() -> saveData(data));
    }

    private void saveData(Data data)
    {
        if (data.getDataId() == null)
        {
            entityManager.persist(data);
        }
        else
        {
            entityManager.merge(data);
        }
    }
}
