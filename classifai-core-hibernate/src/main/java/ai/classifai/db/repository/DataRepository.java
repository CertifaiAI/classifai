package ai.classifai.db.repository;

import ai.classifai.core.entities.traits.HasId;
import ai.classifai.db.entities.annotation.AnnotationEntity;
import ai.classifai.db.handler.DataVersionHandler;
import ai.classifai.db.entities.data.DataEntity;
import ai.classifai.db.entities.dataVersion.DataVersionEntity;

import javax.persistence.EntityManager;
import java.util.List;

public class DataRepository extends Repository<HasId<R>, S, R> {
    public DataRepository(EntityManager entityManager) {
        super(entityManager);
    }

    public void saveDataList(List<DataEntity> dataEntityList)
    {
        List<DataVersionEntity> dataVersionEntityList = DataVersionHandler.getDataVersionListFromDataList(dataEntityList);
        List<AnnotationEntity> annotationEntityList = DataVersionEntity.getAnnotationListFromDataVersionList(dataVersionEntityList);

        // save data
        saveItem(dataEntityList.toArray());

        // save dataversion
        saveItem(dataVersionEntityList.toArray());

        // save annotation
        saveItem(annotationEntityList.toArray());
    }
}
