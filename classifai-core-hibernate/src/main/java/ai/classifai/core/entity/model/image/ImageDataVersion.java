package ai.classifai.core.entity.model.image;

import ai.classifai.core.entity.model.generic.DataVersion;

/**
 * ImageDataVersion entity interface
 *
 * @author YinChuangSum
 */
public interface ImageDataVersion extends DataVersion
{
    Float getImgX();

    Float getImgY();

    Float getImgW();

    Float getImgH();
}
