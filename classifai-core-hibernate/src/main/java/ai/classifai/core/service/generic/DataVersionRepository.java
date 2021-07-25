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

public interface DataVersionRepository<Entity extends DataVersion,
        DataType extends Data, DataTypeDTO extends DataDTO, DTO extends DataVersionDTO>
        extends Repository<Entity, DTO, DataVersion.DataVersionId>
{
    List<Entity> updateList(List<Entity> dataVersionList, List<DTO> dtoList);

    List<Entity> listByVersion(Version version);

    List<Entity> listByData(DataType data);

    Entity getByDataAndVersion(DataType data, Version version);
}
