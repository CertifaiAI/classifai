package ai.classifai.database.repository.generic;

import ai.classifai.core.entity.dto.generic.DataDTO;
import ai.classifai.core.entity.dto.generic.DataVersionDTO;
import ai.classifai.core.entity.model.generic.Data;
import ai.classifai.core.entity.model.generic.DataVersion;
import ai.classifai.core.service.generic.DataVersionRepository;
import ai.classifai.database.repository.generic.AbstractHibernateRepository;

import javax.persistence.EntityManager;

public abstract class DataVersionHibernateRepository<DataVersionType extends DataVersion, DTO extends DataVersionDTO, EntityImpl extends DataVersionType,
        DataType extends Data, DataTypeDTO extends DataDTO>
        extends AbstractHibernateRepository<DataVersionType, DTO, DataVersion.DataVersionId, EntityImpl>
        implements DataVersionRepository<DataVersionType, DataType, DataTypeDTO, DTO>
{

    public DataVersionHibernateRepository(EntityManager em, Class<? extends EntityImpl> entityClass) {
        super(em, entityClass);
    }
}
