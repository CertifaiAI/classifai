package ai.classifai.database.model.annotation;

import ai.classifai.database.model.annotation.ImageAnnotation.BoundingBoxAnnotation;
import ai.classifai.database.model.dataVersion.DataVersion;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class AnnotationListFactory
{
    public AnnotationListFactory() {}

    public List<Annotation> getAnnotationListFromJson(JsonObject request, DataVersion dataVersion)
    {
        if (request.containsKey(BoundingBoxAnnotation.META_KEY))
        {
            List<JsonObject> jsonList = getAnnotationJsonList(request.getJsonArray(BoundingBoxAnnotation.META_KEY));
            return IntStream.range(0, jsonList.size())
                    .mapToObj(i ->
                            BoundingBoxAnnotation.getAnnotationFromJson(jsonList.get(i), dataVersion, i))
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
