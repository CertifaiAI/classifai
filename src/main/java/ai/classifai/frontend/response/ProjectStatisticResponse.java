package ai.classifai.frontend.response;

import ai.classifai.core.properties.LabelNameAndCountProperties;
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
