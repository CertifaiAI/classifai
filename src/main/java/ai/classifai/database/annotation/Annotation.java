package ai.classifai.database.annotation;

public class Annotation {
    String projectId;
    Integer fileSize;

    Annotation(String projectId, Integer fileSize) {
        this.projectId = projectId;
        this.fileSize = fileSize;
    }

}
