package ai.classifai.frontend.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UpdateTabularDataBody {
    @JsonProperty("uuid")
    String uuid;

    @JsonProperty("label")
    String label;
}
