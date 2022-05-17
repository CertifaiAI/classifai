package ai.classifai.database.annotation;

public class AudioAnnotation  {
    String uuid;
    Integer fileSize;

    AudioAnnotation(String uuid, Integer fileSize) {
        this.uuid = uuid;
        this.fileSize = fileSize;
    }

}
