package ai.classifai.service;

import ai.classifai.core.entity.dto.generic.AnnotationDTO;
import ai.classifai.core.entity.dto.image.annotation.BoundingBoxAnnotationDTO;
import ai.classifai.core.entity.dto.image.annotation.PolygonAnnotationDTO;
import ai.classifai.core.entity.model.generic.Annotation;
import ai.classifai.util.type.AnnotationType;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ImageAnnotationService extends AbstractVertxService
{
    public ImageAnnotationService(Vertx vertx)
    {
        super(vertx);
    }


    // factory method
    public List<AnnotationDTO> getAnnotationDTOFromJsonArray(JsonArray annotationJsonArray, AnnotationType type)
    {
        List<JsonObject> jsonList = getAnnotationJsonList(annotationJsonArray);

        switch (type)
        {
            case BOUNDINGBOX -> {
                return IntStream.range(0, jsonList.size())
                        .mapToObj(i -> BoundingBoxAnnotationDTO.builder().build())
                        .collect(Collectors.toList());
            }
            case SEGMENTATION -> {
                return IntStream.range(0, jsonList.size())
                        .mapToObj(i -> PolygonAnnotationDTO.builder().build())
                        .collect(Collectors.toList());
            }
            default -> throw new IllegalArgumentException("Annotation key not found!");
        }
    }

    private List<JsonObject> getAnnotationJsonList(JsonArray arr)
    {
        return arr.stream()
                .map(obj -> (JsonObject) obj)
                .collect(Collectors.toList());
    }

    public Future<List<Annotation>> getToDeleteAnnotationListFuture(List<Annotation> Annotations, List<AnnotationDTO> annotationDTOList)
    {
        return null;
    }

    public Future<List<AnnotationDTO>> getToAddAnnotationListFuture(List<Annotation> Annotations, List<AnnotationDTO> annotationDTOList)
    {
        return null;
    }

    // FIXME: now update is perform here and only persist in dbservice, must be fixed after splitting methods
    public Future<List<Annotation>> getToUpdateAnnotationListFuture(List<Annotation> Annotations, List<AnnotationDTO> annotationDTOList)
    {
        return null;
    }
}
