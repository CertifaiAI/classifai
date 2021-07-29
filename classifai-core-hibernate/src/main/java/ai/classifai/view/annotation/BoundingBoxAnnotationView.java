package ai.classifai.view.annotation;

import ai.classifai.core.entity.dto.generic.AnnotationDTO;
import ai.classifai.core.entity.dto.generic.PointDTO;
import ai.classifai.core.entity.dto.image.annotation.BoundingBoxAnnotationDTO;
import ai.classifai.core.entity.dto.image.annotation.PolygonAnnotationDTO;
import ai.classifai.util.type.AnnotationType;
import io.vertx.core.json.JsonObject;

import java.util.Arrays;
import java.util.List;

/**
 * class for handling bounding box annotation request and response
 *
 * @author YinChuangSum
 */
public class BoundingBoxAnnotationView extends AnnotationView
{

    public BoundingBoxAnnotationView(AnnotationType annotationType)
    {
        super(annotationType);
    }

    @Override
    public JsonObject getSingleAnnotationPointView(List<PointDTO> pointDTOList)
    {
        JsonObject distanceToImg = new JsonObject()
                .put("x", pointDTOList.get(0).getDist2ImgX())
                .put("y", pointDTOList.get(0).getDist2ImgY());

        return new JsonObject()
                .put("x1", pointDTOList.get(0).getX())
                .put("y1", pointDTOList.get(0).getY())
                .put("x2", pointDTOList.get(1).getX())
                .put("y2", pointDTOList.get(1).getY())
                .put("distancetoImg", distanceToImg);
    }

    @Override
    protected List<PointDTO> decodePointLists(JsonObject view)
    {
        JsonObject distanceToImg = view.getJsonObject("distancetoImg");

        PointDTO point1 = PointDTO.builder()
                .x(view.getFloat("x1"))
                .y(view.getFloat("y1"))
                .dist2ImgX(distanceToImg.getFloat("x"))
                .dist2ImgY(distanceToImg.getFloat("y"))
                .position(0)
                .build();

        PointDTO point2 = PointDTO.builder()
                .x(view.getFloat("x2"))
                .y(view.getFloat("y2"))
                .position(1)
                .build();

        return Arrays.asList(point1, point2);
    }

    @Override
    protected AnnotationDTO decodeAnnotation(JsonObject view)
    {
        return BoundingBoxAnnotationDTO.builder()
                .id(view.getLong("id"))
                .build();
    }
}
