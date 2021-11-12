package ai.classifai.core.entities.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CreateProjectDTO {
    @JsonProperty("annotation_type")
    String annotationType;

    @JsonProperty("label_file_path")
    String labelFilePath;

    @JsonProperty("project_name")
    String projectName;

    @JsonProperty("project_path")
    String projectPath;

    @JsonProperty
    String status;
}
