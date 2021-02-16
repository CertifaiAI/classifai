package ai.classifai.action.parser;

import io.vertx.core.json.JsonObject;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BoundingBoxParser {

    private JsonObject content;
}
