package ai.classifai.database.repository.generic;

import ai.classifai.core.entity.dto.generic.DataDTO;
import ai.classifai.core.entity.model.generic.Project;
import ai.classifai.core.entity.model.generic.Data;
import ai.classifai.core.service.generic.DataRepository;
import ai.classifai.database.entity.generic.ProjectEntity;
import ai.classifai.database.entity.generic.DataEntity;
import ai.classifai.database.repository.generic.AbstractHibernateRepository;
import lombok.NonNull;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Class for Data abstract repository with hibernate implementation
 *
 * @author YinChuangSum
 */
public class DataHibernateRepository extends AbstractHibernateRepository<Data, DataDTO, UUID, DataEntity>
        implements DataRepository
{
    public DataHibernateRepository(EntityManager em, Class<? extends DataEntity> entityClass) {
        super(em, entityClass);
    }

    public DataHibernateRepository(EntityManager em)
    {
        super(em, DataEntity.class);
    }
}
