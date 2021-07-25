package ai.classifai.core.service.image;

import ai.classifai.core.entity.model.image.ImageData;
import ai.classifai.core.entity.dto.image.ImageDataDTO;
import ai.classifai.core.service.generic.DataRepository;

public interface ImageDataRepository extends DataRepository<ImageData, ImageDataDTO>
{
}
