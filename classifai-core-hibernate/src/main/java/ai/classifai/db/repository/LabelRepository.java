package ai.classifai.db.repository;

import ai.classifai.core.entities.traits.HasId;
import ai.classifai.db.entities.LabelEntity;

import javax.persistence.EntityManager;
import java.util.List;

public class LabelRepository extends Repository<HasId<R>, S, R> {
    public LabelRepository(EntityManager entityManager) {
        super(entityManager);
    }

    public void saveLabelList(List<LabelEntity> labelEntityList)
    {
        saveItem(labelEntityList.toArray());
    }

    public void removeLabelList(List<LabelEntity> labelEntityList)
    {
        removeItem(labelEntityList.toArray());
    }
}
