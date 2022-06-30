package ai.classifai.frontend.response;

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
public class AddImageResponse {

    @JsonProperty
    int message;

    @JsonProperty("add_image_status")
    int addImageStatus;

    @JsonProperty("add_image_message")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String addImageMessage;

    @JsonProperty("error_message")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String errorMessage;

}
