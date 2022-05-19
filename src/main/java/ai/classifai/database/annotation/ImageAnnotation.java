package ai.classifai.database.annotation;

import ai.classifai.dto.properties.DistanceToImageProperties;
import lombok.Getter;

import java.util.List;

public class ImageAnnotation {
    @Getter private String uuid;
    @Getter private Integer imgDepth;
    @Getter private Integer imgOriW;
    @Getter private Integer imgOriH;
    @Getter private Integer fileSize;

    ImageAnnotation(String uuid, Integer imgDepth, Integer imgOriH, Integer imgOriW, Integer fileSize) {
        this.uuid = uuid;
        this.imgDepth = imgDepth;
        this.imgOriH = imgOriH;
        this.imgOriW = imgOriW;
        this.fileSize = fileSize;
    }

    private class BoundingBox {
        private String id;
        private Integer img_x;
        private Integer img_y;
        private Integer img_w;
        private Integer img_h;
        private String label;
        private List<String> subLabel;
        private String color;
        private String lineWidth;
        private DistanceToImageProperties distanceToImage;

        BoundingBox() {}
    }

    private class Segmentation {
        Segmentation() {}
    }

}
