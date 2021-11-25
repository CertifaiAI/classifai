package ai.classifai.dto.api.response;

import ai.classifai.dto.data.LabelNameAndCountProperties;
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
public class ProjectStatisticResponse {
    @JsonProperty
    int message;

    @JsonProperty("labeled_image")
    int numLabeledImage;

    @JsonProperty("unlabeled_image")
    int numUnLabeledImage;

    @JsonProperty("label_per_class_in_project")
    List<LabelNameAndCountProperties> labelPerClassInProject;

    @JsonProperty("error_message")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String errorMessage;
}
