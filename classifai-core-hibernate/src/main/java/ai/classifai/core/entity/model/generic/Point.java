package ai.classifai.core.entity.model.generic;

import ai.classifai.core.entity.dto.generic.PointDTO;
import ai.classifai.core.entity.trait.HasDTO;
import ai.classifai.core.entity.trait.HasId;

import java.util.UUID;

/**
 * Point entity interface
 *
 * @author YinChuangSum
 */
public interface Point extends HasDTO<PointDTO>, HasId<UUID>
{
    Float getX();

    Float getY();

    Float getDist2ImgX();

    Float getDist2ImgY();

    Integer getPosition();
}
