package ai.classifai.frontend.request;

import ai.classifai.core.dto.properties.BoundingBoxProperties;
import ai.classifai.core.dto.properties.ImageProperties;
import lombok.Value;

@Value
public class ImageAnnotationBody {
    ImageProperties imageProperties;

    BoundingBoxProperties boundingBoxProperties;
}
