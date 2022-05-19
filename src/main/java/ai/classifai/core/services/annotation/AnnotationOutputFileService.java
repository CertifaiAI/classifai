package ai.classifai.core.services.annotation;

import lombok.NonNull;

public interface AnnotationOutputFileService {
    void saveAnnotationFile(@NonNull String projectId);
}
