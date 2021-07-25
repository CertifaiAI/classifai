package ai.classifai.util.data;

import ai.classifai.core.entity.model.generic.Annotation;
import ai.classifai.database.entity.generic.AnnotationEntity;

import java.util.List;

public class AnnotationHandler {
    public List<AnnotationEntity> getDeleteList(List<Annotation> newList, List<Annotation> currentList)
    {
        return null;
//        return currentList.stream()
//                .filter(annotation -> !newList.contains(annotation))
//                .collect(Collectors.toList());
    }
}
