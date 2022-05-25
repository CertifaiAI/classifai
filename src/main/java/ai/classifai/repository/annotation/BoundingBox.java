package ai.classifai.repository.annotation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.util.List;

@Builder
@NonNull
@Data
@AllArgsConstructor
public class BoundingBox {
    private String uuid;
    private int img_x;
    private int img_y;
    private int img_w;
    private int img_h;
    private String label;
    private List<String> subLabel;
    private String color;
    private String lineWidth;
}
