package ai.classifai.db.entities.annotation;

import ai.classifai.db.entities.annotation.ImageAnnotation.BoundingBoxAnnotationEntity;
import ai.classifai.db.entities.dataVersion.DataVersionEntity;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AnnotationListFactory
{
    public AnnotationListFactory() {}

    public List<AnnotationEntity> getAnnotationListFromJson(JsonObject request, DataVersionEntity dataVersionEntity)
    {
        if (request.containsKey(BoundingBoxAnnotationEntity.META_KEY))
        {
            List<JsonObject> jsonList = getAnnotationJsonList(request.getJsonArray(BoundingBoxAnnotationEntity.META_KEY));
            return IntStream.range(0, jsonList.size())
                    .mapToObj(i ->
                            BoundingBoxAnnotationEntity.getAnnotationFromJson(jsonList.get(i), dataVersionEntity, i))
                    .collect(Collectors.toList());
        }
        else
        {
            throw new IllegalArgumentException("Annotation key not found!");
        }
    }

    private List<JsonObject> getAnnotationJsonList(JsonArray arr)
    {
        return arr.stream()
                .map(obj -> (JsonObject) obj)
                .collect(Collectors.toList());
    }
}
