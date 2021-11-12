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
public class DeleteProjectDataResponse {
    @JsonProperty
    int message;

    @JsonProperty("error_message")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String errorMessage;

    @JsonProperty("uuid_list")
    List<String> uuidList;
}
