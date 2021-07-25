package ai.classifai.core.service.generic;

import ai.classifai.core.entity.model.generic.Label;
import ai.classifai.core.entity.dto.generic.LabelDTO;
import ai.classifai.core.entity.model.generic.Version;

import java.util.List;
import java.util.UUID;

public interface LabelRepository extends Repository<Label, LabelDTO, UUID>
{
    List<Label> listByVersion(Version version);
}
