package ai.classifai.core.service.image;

import ai.classifai.core.entity.dto.generic.DataDTO;
import ai.classifai.core.entity.model.generic.Data;
import ai.classifai.core.entity.model.image.ImageData;
import ai.classifai.core.entity.dto.image.ImageDataDTO;
import ai.classifai.core.service.generic.DataRepository;
import ai.classifai.core.service.generic.Repository;

import java.util.UUID;

public interface ImageDataRepository extends DataRepository, Repository<Data, DataDTO, UUID>
{
}
