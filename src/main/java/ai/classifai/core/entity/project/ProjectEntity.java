package ai.classifai.core.entity.project;

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
    String projectName;
    String projectId;
    Integer projectType;
    String projectPath;
    Integer projectInfra;
    List<String> labelList;

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
