package ai.classifai.core.service.annotation;

import ai.classifai.core.dto.TabularDTO;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.util.List;

public interface TabularAnnotationService<T, U> extends AnnotationService<T, U> {
    Future<List<JsonObject>> toJson(List<TabularDTO> tabularDTO);

    Future<List<String>> getAllInvalidData(String projectName);

    Future<Void> writeFile(String projectId, String fileType, boolean isFilterInvalidData);
}
