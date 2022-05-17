package ai.classifai.database.annotation;

public class ImageAnnotation {
    private String uuid;
    private Integer imgDepth;
    private Integer imgOriW;
    private Integer imgOriH;
    private Integer fileSize;

    ImageAnnotation(String uuid, Integer imgDepth, Integer imgOriH, Integer imgOriW, Integer fileSize) {
        this.uuid = uuid;
        this.imgDepth = imgDepth;
        this.imgOriH = imgOriH;
        this.imgOriW = imgOriW;
        this.fileSize = fileSize;
    }

}
