package ai.classifai.core.entity.project;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.sqlclient.Tuple;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;

import java.util.List;

@NonNull
@SuperBuilder
@NoArgsConstructor
@Data
public class ProjectEntity {
    @JsonProperty("project_name")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String projectName;

    @JsonProperty("project_id")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String projectId;

    @JsonProperty("annotation_type")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Integer projectType;

    @JsonProperty("project_path")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    String projectPath;

    @JsonProperty("project_infra")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    Integer projectInfra;

    @JsonProperty("label_list")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    List<String> labelList;

    public Tuple getTuple() {
        return Tuple.of(
                projectId,
                projectName,
                projectType,
                projectPath,
                projectInfra,
                labelList.toString()
        );
    }

    @Override
    public String toString() {
        return "Project{" +
                "projectName='" + projectName + '\'' +
                ", projectId='" + projectId + '\'' +
                ", projectType=" + projectType +
                ", projectPath='" + projectPath + '\'' +
                ", projectInfra=" + projectInfra +
                ", labelList=" + labelList +
                '}';
    }
}
