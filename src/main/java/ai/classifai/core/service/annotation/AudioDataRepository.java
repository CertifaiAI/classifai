package ai.classifai.core.service.annotation;

import ai.classifai.core.loader.ProjectLoader;
import ai.classifai.core.properties.audio.AudioRegionsProperties;
import io.vertx.core.Future;
import lombok.NonNull;

import java.util.List;

public interface AudioDataRepository<T, U> extends AnnotationRepository<T, U> {
    Future<List<Integer>> getWaveFormPeaks(@NonNull ProjectLoader projectLoader);

    Future<Void> saveWaveFormPeaks(String projectId, Double timeStamp, Integer peak, String dataPath);

    Future<List<AudioRegionsProperties>> getAudioRegions(@NonNull String projectId);

    Future<Void> exportAudioAnnotation(@NonNull ProjectLoader projectLoader);
}
