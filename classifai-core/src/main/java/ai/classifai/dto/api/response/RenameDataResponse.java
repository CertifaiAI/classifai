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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RenameDataResponse {
    @JsonProperty
    int message;

    @JsonProperty("error_code")
    int errorCode;

    @JsonProperty("error_message")
    String errorMessage;

    @JsonProperty("img_path")
    String imgPath;
}
