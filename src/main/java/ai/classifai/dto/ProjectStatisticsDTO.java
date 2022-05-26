package ai.classifai.dto;

import ai.classifai.dto.properties.LabelNameAndCount;
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
    List<LabelNameAndCount> labelPerClassInProject;

    @JsonProperty("error_message")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String errorMessage;
}
