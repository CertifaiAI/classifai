package ai.classifai.service.image;

import ai.classifai.core.entity.dto.generic.AnnotationDTO;
import ai.classifai.core.entity.dto.generic.LabelDTO;
import ai.classifai.core.entity.model.generic.Annotation;
import ai.classifai.core.entity.model.generic.Label;
import ai.classifai.service.generic.AbstractVertxService;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ImageAnnotationService extends AbstractVertxService
{
    public ImageAnnotationService(Vertx vertx)
    {
        super(vertx);
    }

    // FIXME: temporarily code for current frontend
    public Future<Annotation> getToDeleteAnnotationFuture(List<Annotation> annotationList, List<AnnotationDTO> dtoList)
    {
        if (dtoList.size() >= annotationList.size()) return Future.succeededFuture();

        List<Long> annotationIdList = dtoList.stream()
                .map(AnnotationDTO::getId)
                .collect(Collectors.toList());

        Annotation toDelete = annotationList.stream()
                .filter(annotation -> !annotationIdList.contains(annotation.getId()))
                .findFirst()
                .get();

        return Future.succeededFuture(toDelete);
    }

    // FIXME: temporarily code for current frontend
    public Future<AnnotationDTO> getToAddAnnotationFuture(List<Annotation> annotationList, List<AnnotationDTO> dtoList)
    {
        if (dtoList.size() <= annotationList.size()) return Future.succeededFuture();

        return Future.succeededFuture(dtoList.get(dtoList.size() - 1)); // get the last one
    }

    // FIXME: temporarily code for current frontend
    public Future<UpdateAnnotationLabelObject> getToUpdateLabelAnnotationFuture(List<Annotation> annotationList, List<LabelDTO> labelDTOList, List<Label> labelList)
    {
        return vertx.executeBlocking(promise ->
        {
            if ((annotationList.size() == 0 && labelDTOList.size() == 0)|| annotationList.size() != labelDTOList.size())
            {
                promise.complete();
                return;
            }

            promise.complete(IntStream.range(0, annotationList.size())
                    .mapToObj(idx ->
                    {
                        String initialLabelName = annotationList.get(idx).getLabel().getName();
                        String updateLabelName = labelDTOList.get(idx).getName();
                        if (initialLabelName.equals(updateLabelName)) return null;

                        Label labelToUpdate = labelList.stream()
                                .filter(label -> label.getName().equals(updateLabelName))
                                .findFirst()
                                .get();

                        Annotation annotation = annotationList.get(idx);

                        return new UpdateAnnotationLabelObject(annotation, labelToUpdate);
                    }).filter(Objects::nonNull)
                    .findFirst()
                    .orElse(null));
        });
    }

    @Data
    @AllArgsConstructor
    // FIXME: temporarily code for current frontend
    public static class UpdateAnnotationLabelObject
    {
        Annotation annotation;
        Label label;
    }
}
