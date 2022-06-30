package ai.classifai.frontend.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileSysStatusResponse {
    @JsonProperty
    int message;

    @JsonProperty("error_message")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String errorMessage;

    @JsonProperty("file_system_status")
    int fileSystemStatus;

    @JsonProperty("file_system_message")
    String fileSystemMessage;

    @JsonProperty("unsupported_image_list")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    List<String> unsupportedImageList;

    @JsonProperty("project_name")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String projectName;
}
