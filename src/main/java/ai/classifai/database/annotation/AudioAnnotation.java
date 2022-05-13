package ai.classifai.database.annotation;

public class AudioAnnotation extends Annotation implements AnnotationProps {
    String audioProperties;
    Integer frameSize;
    Integer frameRate;
    Integer channels;
    String bits;
    Integer sampleRate;

    AudioAnnotation(String projectId, Integer fileSize) {
        super(projectId, fileSize);
    }

    public String getAnnotationProperties() {
        return audioProperties;
    }
}
