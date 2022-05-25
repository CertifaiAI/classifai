package ai.classifai.repository.annotation;

public class VideoAnnotation {
    String uuid;
    int fileSize;

    VideoAnnotation(String uuid, Integer fileSize)
    {
        this.uuid = uuid;
        this.fileSize = fileSize;
    }

}
