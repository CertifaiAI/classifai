package ai.classifai.dto.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportStatusResponse {
    @JsonProperty
    int message;

    @JsonProperty("export_status")
    int exportStatus;

    @JsonProperty("export_status_message")
    String exportStatusMessage;

    @JsonProperty("project_config_path")
    String projectConfigPath;
}
