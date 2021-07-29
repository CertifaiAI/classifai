package ai.classifai.view.dataversion;

import ai.classifai.core.entity.dto.generic.DataVersionDTO;
import ai.classifai.util.type.AnnotationType;
import io.vertx.core.json.JsonObject;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * abstract class for handling data version request and response
 *
 * @author YinChuangSum
 */
@Data
@NoArgsConstructor
public abstract class DataVersionView
{
    protected DataVersionDTO dto;

    public static DataVersionView getDataVersionView(AnnotationType annotationType)
    {
        return switch(annotationType)
                {
                    case BOUNDINGBOX, SEGMENTATION -> new ImageDataVersionView();
                };
    }

    public abstract JsonObject generateImageDataVersionView(DataVersionDTO dataVersionDTO);

    public abstract void decode(JsonObject requestBody);
}
