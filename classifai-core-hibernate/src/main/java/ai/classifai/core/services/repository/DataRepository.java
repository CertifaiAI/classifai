package ai.classifai.core.services.repository;

import ai.classifai.core.entities.Data;
import ai.classifai.core.entities.dto.DataDTO;

import java.util.UUID;

public interface DataRepository extends Repository<Data, DataDTO, UUID>
{
}
