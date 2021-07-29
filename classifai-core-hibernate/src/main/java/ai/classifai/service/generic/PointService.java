package ai.classifai.service.generic;

import ai.classifai.core.entity.dto.generic.PointDTO;
import ai.classifai.core.entity.model.generic.Point;
import ai.classifai.service.generic.AbstractVertxService;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PointService extends AbstractVertxService
{
    public PointService(Vertx vertx)
    {
        super(vertx);
    }

    // FIXME: temporarily code for current frontend
    public Future<UpdatePointObject> getToUpdatePointFuture(List<List<Point>> pointLists, List<List<PointDTO>> dtoLists)
    {
        return vertx.executeBlocking(promise ->
        {
            if ((dtoLists.size() == 0 && pointLists.size() == 0) || pointLists.size() != dtoLists.size())
            {
                promise.complete();
                return;
            }

            promise.complete(IntStream.range(0, pointLists.size())
                    .mapToObj(idx -> getDiffPoint(pointLists.get(idx), dtoLists.get(idx)))
                    .filter(Objects::nonNull)
                    .findFirst()
                    .get());
        });
    }

    // FIXME: temporarily code for current frontend
    private UpdatePointObject getDiffPoint(List<Point> pointList, List<PointDTO> dtoList)
    {
        List<PointDTO> pointDTOList = pointList.stream()
                .map(Point::toDTO)
                .collect(Collectors.toList());;

        for (int i = 0; i < pointDTOList.size(); i++)
        {
            if (! pointDTOList.get(i).equals(dtoList.get(i)))
            {
                return new UpdatePointObject(pointList.get(i), dtoList.get(i));
            }
        }

        return null;
    }

    @Data
    @AllArgsConstructor
    // FIXME: temporarily code for current frontend
    public static class UpdatePointObject
    {
        Point point;
        PointDTO dto;
    }
}
