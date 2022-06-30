package ai.classifai.core.service.annotation;

import ai.classifai.core.loader.ProjectLoader;
import ai.classifai.frontend.request.ThumbnailProperties;
import io.vertx.core.Future;
import lombok.NonNull;

public interface ImageAnnotationService<T, U> extends AnnotationService<T, U> {
    Future<ThumbnailProperties> getThumbnail(@NonNull ProjectLoader projectLoader, @NonNull String uuid);

    Future<String> getImageSource(@NonNull ProjectLoader projectLoader, @NonNull String uuid);
}
