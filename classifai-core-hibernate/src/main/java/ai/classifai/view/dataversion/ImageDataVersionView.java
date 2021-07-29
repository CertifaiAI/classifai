package ai.classifai.view.dataversion;

import ai.classifai.core.entity.dto.generic.DataVersionDTO;
import ai.classifai.core.entity.dto.image.ImageDataVersionDTO;
import io.vertx.core.json.JsonObject;

/**
 * class for handling image data version request and response
 *
 * @author YinChuangSum
 */
public class ImageDataVersionView extends DataVersionView
{
    @Override
    public JsonObject generateImageDataVersionView(DataVersionDTO dataVersionDTO)
    {
        ImageDataVersionDTO dto = ImageDataVersionDTO.toDTOImpl(dataVersionDTO);

        return new JsonObject()
                .put("img_x", dto.getImgX())
                .put("img_y", dto.getImgY())
                .put("img_w", dto.getImgW())
                .put("img_h", dto.getImgH());
    }

    @Override
    public void decode(JsonObject view)
    {
        dto = ImageDataVersionDTO.builder()
                .imgX(view.getFloat("img_x"))
                .imgY(view.getFloat("img_y"))
                .imgW(view.getFloat("img_w"))
                .imgH(view.getFloat("img_h"))
                .build();
    }
}
