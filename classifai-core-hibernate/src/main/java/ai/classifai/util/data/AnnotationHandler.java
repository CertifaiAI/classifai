package ai.classifai.util.data;

import ai.classifai.db.entities.annotation.AnnotationEntity;

import java.util.List;
import java.util.stream.Collectors;

public class AnnotationHandler {
    public List<AnnotationEntity> getDeleteList(List<AnnotationEntity> newList, List<AnnotationEntity> currentList)
    {
        return currentList.stream()
                .filter(annotation -> !newList.contains(annotation))
                .collect(Collectors.toList());
    }
}
