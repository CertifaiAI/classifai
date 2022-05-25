package ai.classifai.client.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CreateProjectBody {
    @JsonProperty
    String status;

    @JsonProperty("annotation_type")
    String annotationType;

    @JsonProperty("project_name")
    String projectName;

    @JsonProperty("project_path")
    String projectPath;

    @JsonProperty("label_file_path")
    String labelFilePath;

    @JsonProperty("project_infra")
    String projectInfra;
}
