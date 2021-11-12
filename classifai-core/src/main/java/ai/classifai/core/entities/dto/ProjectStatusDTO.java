package ai.classifai.core.entities.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ProjectStatusDTO {
    @JsonProperty
    String status;
}
