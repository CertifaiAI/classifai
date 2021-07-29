package ai.classifai.core.service.image;

import ai.classifai.core.entity.dto.generic.DataVersionDTO;
import ai.classifai.core.entity.dto.image.ImageDataDTO;
import ai.classifai.core.entity.dto.image.annotation.ImageAnnotationDTO;
import ai.classifai.core.entity.model.generic.DataVersion;
import ai.classifai.core.entity.model.image.ImageData;
import ai.classifai.core.entity.model.image.annotation.ImageAnnotation;
import ai.classifai.core.entity.model.image.ImageDataVersion;
import ai.classifai.core.entity.dto.image.ImageDataVersionDTO;
import ai.classifai.core.service.generic.DataVersionRepository;
import ai.classifai.core.service.generic.Repository;

/**
 * Repository ImageDataVersion of Annotation entity
 *
 * @author YinChuangSum
 */
public interface ImageDataVersionRepository extends DataVersionRepository, Repository<DataVersion, DataVersionDTO, DataVersion.DataVersionId>
{}
