package ai.classifai.dto.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SelectionStatusResponse {
    @JsonProperty
    int message;

    @JsonProperty("window_status")
    int windowStatus;

    @JsonProperty("window_message")
    String windowMessage;

    @JsonProperty("project_path")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String projectPath;

    @JsonProperty("label_file_path")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String labelFilePath;
}
