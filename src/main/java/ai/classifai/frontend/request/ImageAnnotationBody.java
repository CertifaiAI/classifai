package ai.classifai.frontend.request;

import ai.classifai.backend.dto.properties.BoundingBoxProperties;
import ai.classifai.backend.dto.properties.ImageProperties;
import lombok.Value;

@Value
public class ImageAnnotationBody {
    ImageProperties imageProperties;
    BoundingBoxProperties boundingBoxProperties;
}
