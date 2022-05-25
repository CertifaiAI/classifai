package ai.classifai.dto;

import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@NonNull
public class BoundingBoxDTO {
    String uuid;

    int img_x;

    int img_y;

    int img_w;

    int img_h;

    String label;

    List<String> subLabel;

    String color;

    String lineWidth;

}
