package ai.classifai.dto.api.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@NonNull
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoadingStatusResponse {
    @JsonProperty
    int message;

    @JsonProperty("error_message")
    String errorMessage;

    @JsonProperty
    List<Integer> progress;

    @JsonProperty("label_list")
    List<String> labelList;

    @JsonProperty("uuid_list")
    List<String> sanityUuidList;
}
