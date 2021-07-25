package ai.classifai.core.service.image;

import ai.classifai.core.entity.dto.image.ImageDataDTO;
import ai.classifai.core.entity.dto.image.annotation.ImageAnnotationDTO;
import ai.classifai.core.entity.model.image.ImageData;
import ai.classifai.core.entity.model.image.annotation.ImageAnnotation;
import ai.classifai.core.entity.model.image.ImageDataVersion;
import ai.classifai.core.entity.dto.image.ImageDataVersionDTO;
import ai.classifai.core.service.generic.DataVersionRepository;

public interface ImageDataVersionRepository extends DataVersionRepository<ImageDataVersion, ImageData, ImageDataDTO, ImageDataVersionDTO>
{}
