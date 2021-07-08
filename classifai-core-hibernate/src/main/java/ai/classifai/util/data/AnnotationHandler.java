package ai.classifai.util.data;

import ai.classifai.database.model.annotation.Annotation;

import java.util.List;
import java.util.stream.Collectors;

public class AnnotationHandler {
    public List<Annotation> getDeleteList(List<Annotation> newList, List<Annotation> currentList)
    {
        return currentList.stream()
                .filter(annotation -> !newList.contains(annotation))
                .collect(Collectors.toList());
    }
}
