package ai.classifai.core.entities;

import ai.classifai.core.entities.dto.LabelDTO;
import ai.classifai.core.entities.traits.HasDTO;
import ai.classifai.core.entities.traits.HasId;

import java.util.UUID;

public interface Label extends HasId<UUID>, HasDTO<LabelDTO>
{
    String getValue();
    String getColor();
}
