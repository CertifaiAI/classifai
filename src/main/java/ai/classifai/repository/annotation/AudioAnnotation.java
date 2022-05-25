package ai.classifai.repository.annotation;

public class AudioAnnotation  {
    String uuid;
    int fileSize;

    AudioAnnotation(String uuid, Integer fileSize) {
        this.uuid = uuid;
        this.fileSize = fileSize;
    }

}
