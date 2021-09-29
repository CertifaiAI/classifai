package ai.classifai.dto.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@NonNull
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ImageSourceResponse {
    @JsonProperty
    int message;

    @JsonProperty("error_message")
    String errorMessage;

    @JsonProperty("img_src")
    String imgSrc;
}
