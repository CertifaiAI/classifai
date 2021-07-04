package ai.classifai.database.repository;

import ai.classifai.database.model.annotation.Annotation;
import ai.classifai.database.model.data.Data;
import ai.classifai.database.model.dataVersion.DataVersion;

import javax.persistence.EntityManager;
import java.util.List;

public class DataRepository extends Repository
{
    public DataRepository(EntityManager entityManager) {
        super(entityManager);
    }

    public void saveDataList(List<Data> dataList)
    {
        List<DataVersion> dataVersionList = Data.getDataVersionListFromDataList(dataList);
        List<Annotation> annotationList = DataVersion.getAnnotationListFromDataVersionList(dataVersionList);

        // save data
        saveItem(dataList.toArray());

        // save dataversion
        saveItem(dataVersionList.toArray());

        // save annotation
        saveItem(annotationList.toArray());
    }
}
