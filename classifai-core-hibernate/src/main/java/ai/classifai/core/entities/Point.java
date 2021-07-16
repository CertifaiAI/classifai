package ai.classifai.core.entities;

import ai.classifai.core.entities.dto.PointDTO;
import ai.classifai.core.entities.traits.HasDTO;
import ai.classifai.core.entities.traits.HasId;
import ai.classifai.core.entities.traits.HasOrder;

import java.util.UUID;

public interface Point extends HasId<UUID>, HasDTO<PointDTO>, HasOrder
{
    Float getX();
    Float getY();
    Float getDist2ImgX();
    Float getDist2ImgY();
}
