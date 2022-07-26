package ai.classifai.core.service.annotation;

import ai.classifai.core.dto.TabularDTO;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.Optional;

public interface TabularAnnotationService<T, U> extends AnnotationService<T, U> {
    Future<List<JsonObject>> toJson(List<TabularDTO> tabularDTO);

    Future<List<String>> getAllInvalidData(String projectName);

    Future<Void> writeFile(String projectId, String fileType, boolean isFilterInvalidData);

    Future<Optional<TabularDTO>> automateTabularLabelling(String projectId, JsonObject preLabellingConditions, String currentUuid, String labellingMode);
}
