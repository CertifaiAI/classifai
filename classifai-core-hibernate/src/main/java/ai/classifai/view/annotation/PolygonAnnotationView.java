package ai.classifai.view.annotation;


import ai.classifai.core.entity.dto.generic.AnnotationDTO;
import ai.classifai.core.entity.dto.generic.PointDTO;
import ai.classifai.core.entity.dto.image.annotation.BoundingBoxAnnotationDTO;
import ai.classifai.core.entity.dto.image.annotation.PolygonAnnotationDTO;
import ai.classifai.util.type.AnnotationType;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class PolygonAnnotationView extends AnnotationView
{

    public PolygonAnnotationView(AnnotationType annotationType)
    {
        super(annotationType);
    }

    @Override
    protected JsonObject getSingleAnnotationPointView(List<PointDTO> pointDTOList)
    {
        JsonArray pointListView = new JsonArray();

        pointDTOList.forEach(pointDTO -> pointListView.add(pointToJson(pointDTO)));

        return new JsonObject()
                .put("coorPt", pointListView);
    }

    @Override
    protected List<PointDTO> decodePointLists(JsonObject view)
    {
        List<PointDTO> pointDTOList = new ArrayList<>();

        JsonArray pointViewList = view.getJsonArray("coorPt");

        IntStream.range(0, pointViewList.size())
                .forEach(idx -> {
                        JsonObject pointView = pointViewList.getJsonObject(idx);

                    JsonObject distanceToImg = pointView.getJsonObject("distancetoImg");

                    PointDTO pointDTO = PointDTO.builder()
                            .x(pointView.getFloat("x"))
                            .y(pointView.getFloat("y"))
                            .dist2ImgX(distanceToImg.getFloat("x"))
                            .dist2ImgY(distanceToImg.getFloat("y"))
                            .position(idx)
                            .build();

                    pointDTOList.add(pointDTO);
                });

        return pointDTOList;
    }

    @Override
    protected AnnotationDTO decodeAnnotation(JsonObject view)
    {
        return PolygonAnnotationDTO.builder()
                .id(view.getLong("id"))
                .build();
    }

    private JsonObject pointToJson(PointDTO pointDTO)
    {
        JsonObject distanceToImg = new JsonObject()
                .put("x", pointDTO.getDist2ImgX())
                .put("y", pointDTO.getDist2ImgY());

        return new JsonObject()
                .put("x", pointDTO.getX())
                .put("y", pointDTO.getY())
                .put("distancetoImg", distanceToImg);
    }
}
