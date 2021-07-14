package ai.classifai.entities.dto;

import ai.classifai.database.model.Project;
import io.vertx.core.json.JsonObject;
import lombok.*;

import java.io.Serial;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ProjectDTO extends ExportDTO<ProjectDTO>
{
    @Serial
    private static final long serialVersionUID = -8782409601983389414L;

    UUID id;
    String name;
    String annotationType;
    String path;
    Boolean starred;
    Integer infra;
    String currentVersionId;

    List<UUID> versionIds;
    List<Data> dataIds;

    @Override
    public ProjectDTO readJson(String jsonString)
    {
        JsonObject json = new JsonObject(jsonString);

        return ProjectDTO.builder()
                .name(json.getString(Project.PROJECT_NAME_KEY))
                .annotationType(json.getString(Project.ANNOTATION_TYPE_KEY))
                .currentVersionId(json.getString(Project.CURRENT_VERSION_KEY))
                .build();
    }

    @Override
    public String toJson()
    {
        return new JsonObject()
                .put(Project.PROJECT_NAME_KEY, name)
                .put(Project.ANNOTATION_TYPE_KEY, annotationType)
                .put(Project.CURRENT_VERSION_KEY, currentVersionId)
                .encodePrettily();
    }

}
