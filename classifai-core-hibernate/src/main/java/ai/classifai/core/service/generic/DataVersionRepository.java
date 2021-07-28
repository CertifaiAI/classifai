package ai.classifai.core.service.generic;

import ai.classifai.core.entity.dto.generic.AnnotationDTO;
import ai.classifai.core.entity.dto.generic.DataDTO;
import ai.classifai.core.entity.model.generic.Annotation;
import ai.classifai.core.entity.model.generic.Version;
import ai.classifai.core.entity.model.generic.Data;
import ai.classifai.core.entity.model.generic.DataVersion;
import ai.classifai.core.entity.dto.generic.DataVersionDTO;
import ai.classifai.core.service.generic.Repository;

import java.util.List;

public interface DataVersionRepository extends AbstractRepository<DataVersion, DataVersionDTO, DataVersion.DataVersionId>
{
    DataVersion update(DataVersion dataVersion, DataVersionDTO dto);
}
