package ai.classifai.core.entity.model.generic;

import ai.classifai.core.entity.dto.generic.LabelDTO;
import ai.classifai.core.entity.trait.HasDTO;
import ai.classifai.core.entity.trait.HasId;

import java.util.UUID;

public interface Label extends HasDTO<LabelDTO>, HasId<UUID>
{
    String getName();

    String getColor();
}
