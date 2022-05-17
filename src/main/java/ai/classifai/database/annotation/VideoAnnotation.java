package ai.classifai.database.annotation;

public class VideoAnnotation {
    String uuid;
    Integer fileSize;

    VideoAnnotation(String uuid, Integer fileSize)
    {
        this.uuid = uuid;
        this.fileSize = fileSize;

    }

}
