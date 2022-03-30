package ai.classifai.dto.api.body;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
public class UpdateTabularDataBody {
    @JsonProperty("uuid")
    String uuid;

    @JsonProperty("tabular_label")
    String label;
}
