package ai.classifai.dto.api.body;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RenameDataBody {
    @JsonProperty
    String uuid;

    @JsonProperty("new_fname")
    String newFilename;
}
