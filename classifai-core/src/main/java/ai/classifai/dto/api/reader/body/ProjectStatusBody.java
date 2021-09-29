package ai.classifai.dto.api.reader.body;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ProjectStatusBody {
    @JsonProperty
    String status;
}
