package ai.classifai.core.entity.model.image;

import ai.classifai.core.entity.model.generic.Data;

public interface ImageData extends Data
{
    Integer getDepth();

    Integer getHeight();

    Integer getWidth();
}
