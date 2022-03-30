package ai.classifai.dto.api.body;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TabularFileBody {
    @JsonProperty("file_type")
    String fileType;

    @JsonProperty("filter_invalid_data")
    boolean filterInvalidData;
}
