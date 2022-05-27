package ai.classifai.client.response;

import io.vertx.core.json.JsonObject;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResponseStatus {
    String message;
    String errorMessage;
    JsonObject jsonObject;
}
