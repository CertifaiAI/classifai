package ai.classifai.core.service.generic;

import ai.classifai.core.entity.model.generic.Project;
import ai.classifai.core.entity.model.generic.Data;
import ai.classifai.core.entity.dto.generic.DataDTO;
import ai.classifai.core.service.generic.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Abstract repository of Data entity
 *
 * @author YinChuangSum
 */
public interface DataRepository extends AbstractRepository<Data, DataDTO, UUID>
{
}
