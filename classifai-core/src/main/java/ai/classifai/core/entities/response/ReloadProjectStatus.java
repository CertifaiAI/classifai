package ai.classifai.core.entities.response;

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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReloadProjectStatus {
    @JsonProperty
    int message;

    @JsonProperty("error_message")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String errorMessage;

    @JsonProperty("file_system_status")
    int fileSystemStatus;

    @JsonProperty("file_system_message")
    String fileSystemMessage;

    @JsonProperty
    List<Integer> progress;

    @JsonProperty("uuid_add_list")
    List<String> uuidAddList;

    @JsonProperty("uuid_delete_list")
    List<String> uuidDeleteList;

    @JsonProperty("unsupported_image_list")
    List<String> unsupportedImageList;
}
