package ai.classifai.core.dto;

import ai.classifai.core.properties.LabelNameAndCountProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ProjectStatisticsDTO {
    @JsonProperty
    int message;

    @JsonProperty("labeled_data")
    int numLabeledData;

    @JsonProperty("unlabeled_data")
    int numUnLabeledData;

    @JsonProperty("label_per_class_in_project")
    List<LabelNameAndCountProperties> labelPerClassInProject;

    @JsonProperty("error_message")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String errorMessage;
}
