package ai.classifai.core.entity.annotation;

public class AudioAnnotation  {
    String uuid;
    int fileSize;

    AudioAnnotation(String uuid, Integer fileSize) {
        this.uuid = uuid;
        this.fileSize = fileSize;
    }

}
