package ai.classifai.database.annotation;

public class TabularAnnotation extends Annotation implements AnnotationProps {
    String tabularAnnotationProperties;

    TabularAnnotation(String projectId, Integer fileSize) {
        super(projectId, fileSize);
    }

    public String getAnnotationProperties() {
        return tabularAnnotationProperties;
    }
}
