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
public class ImageAndFolderToProjectResponse {

    @JsonProperty
    int message;

    @JsonProperty("add_image_status")
    int addImageStatus;

    @JsonProperty("add_image_message")
    String addImageMessage;

    @JsonProperty("add_folder_status")
    int addFolderStatus;

    @JsonProperty("add_folder_message")
    String addFolderMessage;

    @JsonProperty("error_message")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String errorMessage;

}
