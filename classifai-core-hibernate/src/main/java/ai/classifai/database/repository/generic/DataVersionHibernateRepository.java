package ai.classifai.database.repository.generic;

import ai.classifai.core.entity.dto.generic.DataDTO;
import ai.classifai.core.entity.dto.generic.DataVersionDTO;
import ai.classifai.core.entity.model.generic.Data;
import ai.classifai.core.entity.model.generic.DataVersion;
import ai.classifai.core.service.generic.DataVersionRepository;
import ai.classifai.database.entity.generic.DataEntity;
import ai.classifai.database.entity.generic.DataVersionEntity;
import ai.classifai.database.repository.generic.AbstractHibernateRepository;
import lombok.NonNull;

import javax.persistence.EntityManager;

public class DataVersionHibernateRepository extends AbstractHibernateRepository<DataVersion, DataVersionDTO, DataVersion.DataVersionId, DataVersionEntity> implements DataVersionRepository
{
    public DataVersionHibernateRepository(EntityManager em, Class<? extends DataVersionEntity> entityClass) {
        super(em, entityClass);
    }

    public DataVersionHibernateRepository(EntityManager em)
    {
        super(em, DataVersionEntity.class);
    }


    @Override
    public DataVersion update(DataVersion dataVersion, DataVersionDTO dto)
    {
        dataVersion.update(dto);
        return null;
    }
}
