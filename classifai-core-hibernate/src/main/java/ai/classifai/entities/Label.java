package ai.classifai.entities;

import ai.classifai.entities.dto.LabelDTO;
import ai.classifai.entities.traits.HasDTO;
import ai.classifai.entities.traits.HasId;

import java.util.UUID;

public interface Label extends HasId<UUID>, HasDTO<LabelDTO>
{
    String getValue();
    String getColor();
}
