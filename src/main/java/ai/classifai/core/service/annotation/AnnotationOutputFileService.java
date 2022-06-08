package ai.classifai.core.service.annotation;

import lombok.NonNull;

public interface AnnotationOutputFileService {
    void saveAnnotationFile(@NonNull String projectId);
}
