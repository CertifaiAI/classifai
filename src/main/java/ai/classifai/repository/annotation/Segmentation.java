package ai.classifai.repository.annotation;

import ai.classifai.dto.properties.CoordinatePointsProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.util.List;

@Builder
@NonNull
@Data
@AllArgsConstructor
public class Segmentation {
    private String uuid;
    private String label;
    private List<String> subLabel;
    private String color;
    private String lineWidth;
    private List<CoordinatePointsProperties> coordinatePointsProperties;

}
