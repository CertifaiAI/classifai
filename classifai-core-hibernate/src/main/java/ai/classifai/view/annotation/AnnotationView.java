package ai.classifai.view.annotation;

import ai.classifai.core.entity.dto.generic.AnnotationDTO;
import ai.classifai.core.entity.dto.generic.LabelDTO;
import ai.classifai.core.entity.dto.generic.PointDTO;
import ai.classifai.util.type.AnnotationType;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Getter
public abstract class AnnotationView
{
    final AnnotationType annotationType;
    protected List<AnnotationDTO> annotationDTOList;
    protected List<List<PointDTO>> pointDTOLists;
    protected List<LabelDTO> labelDTOList;

    protected AnnotationView(AnnotationType annotationType)
    {
        this.annotationType = annotationType;
    }

    public static AnnotationView getAnnotationView(AnnotationType annotationType)
    {
        return switch(annotationType)
        {
            case BOUNDINGBOX -> new BoundingBoxAnnotationView(annotationType);
            case SEGMENTATION -> new PolygonAnnotationView(annotationType);
        };
    }

    public JsonObject generateAnnotationView(List<AnnotationDTO> imageAnnotationDTOList, List<List<PointDTO>> pointDTOLists, List<LabelDTO> labelDTOList)
    {
        JsonArray annotationList = new JsonArray();

        IntStream.range(0, imageAnnotationDTOList.size())
                .forEach(i -> annotationList.add(getSingleAnnotationView(imageAnnotationDTOList.get(i), pointDTOLists.get(i), labelDTOList.get(i))));

        return new JsonObject()
                .put(annotationType.META_KEY, annotationList);
    }

    private JsonObject getSingleAnnotationView(AnnotationDTO annotationDTO, List<PointDTO> pointDTOList, LabelDTO labelDTO)
    {
        return getSingleAnnotationBasicView(annotationDTO, labelDTO)
                .mergeIn(getSingleAnnotationPointView(pointDTOList));
    }

    protected abstract JsonObject getSingleAnnotationPointView(List<PointDTO> pointDTOList);

    private JsonObject getSingleAnnotationBasicView(AnnotationDTO annotationDTO, LabelDTO labelDTO)
    {
        return new JsonObject()
                .put("lineWidth", 1)
                .put("color", labelDTO.getColor())
                .put("label", labelDTO.getName())
                .put("id", annotationDTO.getId());
    }

    public void decode(JsonObject view)
    {
        JsonArray annotationArray = view.getJsonArray(annotationType.META_KEY);

        annotationDTOList = new ArrayList<>();
        pointDTOLists = new ArrayList<>();
        labelDTOList = new ArrayList<>();

        IntStream.range(0, annotationArray.size())
                .forEach(idx ->
                {
                    JsonObject annotationView = annotationArray.getJsonObject(idx);

                    AnnotationDTO annotationDTO = decodeAnnotation(annotationView);
                    annotationDTO.setPosition(idx);
                    annotationDTOList.add(annotationDTO);
                    pointDTOLists.add(decodePointLists(annotationView));
                    labelDTOList.add(decodeLabel(annotationView));
                });
    }

    protected abstract List<PointDTO> decodePointLists(JsonObject view);

    protected abstract AnnotationDTO decodeAnnotation(JsonObject view);

    public LabelDTO decodeLabel(JsonObject view)
    {
        String name = view.getString("label");

        return LabelDTO.builder()
                .name(name)
                .build();
    }

}
