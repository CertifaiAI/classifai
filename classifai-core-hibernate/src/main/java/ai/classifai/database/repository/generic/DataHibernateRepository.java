package ai.classifai.database.repository.generic;

import ai.classifai.core.entity.dto.generic.DataDTO;
import ai.classifai.core.entity.model.generic.Project;
import ai.classifai.core.entity.model.generic.Data;
import ai.classifai.core.service.generic.DataRepository;
import ai.classifai.database.entity.generic.ProjectEntity;
import ai.classifai.database.entity.generic.DataEntity;
import ai.classifai.database.repository.generic.AbstractHibernateRepository;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class DataHibernateRepository<DataType extends Data, DTO extends DataDTO, EntityImpl extends DataType>
        extends AbstractHibernateRepository<DataType, DTO, UUID, EntityImpl> implements DataRepository<DataType, DTO>
{
    public DataHibernateRepository(EntityManager em, Class<? extends EntityImpl> entityClass) {
        super(em, entityClass);
    }


//    public void saveDataList(List<DataEntity> dataList)
//    {
//        List<DataVersionEntity> dataVersionList = DataVersionHandler.getDataVersionListFromDataList(dataList);
//        List<AnnotationEntity> annotationList = DataVersionEntity.getAnnotationListFromDataVersionList(dataVersionList);
//
//        // save data
//        saveItem(dataList.toArray());
//
//        // save dataversion
//        saveItem(dataVersionList.toArray());
//
//        // save annotation
//        saveItem(annotationList.toArray());
//    }

}
