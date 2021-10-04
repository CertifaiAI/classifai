package ai.classifai.dto.api.body;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ProjectStatusBody {
    @JsonProperty
    String status;
}
