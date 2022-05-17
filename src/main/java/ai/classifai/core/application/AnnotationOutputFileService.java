package ai.classifai.core.application;

import lombok.NonNull;

public interface AnnotationOutputFileService {
    void saveAnnotationFile(@NonNull String projectId);
}
