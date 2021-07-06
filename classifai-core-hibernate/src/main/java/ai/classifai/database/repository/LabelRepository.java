package ai.classifai.database.repository;

import ai.classifai.database.model.Label;

import javax.persistence.EntityManager;
import java.util.List;

public class LabelRepository extends Repository{
    public LabelRepository(EntityManager entityManager) {
        super(entityManager);
    }

    public void saveLabelList(List<Label> labelList)
    {
        saveItem(labelList.toArray());
    }

    public void removeLabelList(List<Label> labelList)
    {
        removeItem(labelList.toArray());
    }
}
