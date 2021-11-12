package ai.classifai.core.entities.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RenameDataDTO {
    @JsonProperty
    String uuid;

    @JsonProperty("new_fname")
    String newFilename;
}
