package ai.classifai.core.service.annotation;

import io.vertx.core.Future;

import java.util.List;
import java.util.Map;

public interface TabularDataRepository<T, U> extends AnnotationRepository<T, U>{
    Future<Void> createAndUpdateProjectAttributeTable(String projectId, String columnNames, String attributeTypesJson);

    Future<List<String>> getAttributes(String projectName);

    Future<Map<String,String>> getAttributeTypeMap(String projectName);

    Future<List<String>> getAllInvalidData(String projectName);

    Future<Void> writeFile(String projectId, String fileType, boolean isFilterInvalidData);
}
