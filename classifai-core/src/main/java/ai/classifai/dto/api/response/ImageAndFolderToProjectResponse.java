package ai.classifai.dto.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageAndFolderToProjectResponse {

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
