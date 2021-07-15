package ai.classifai.core.entities.dto;

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
    UUID currentVersionId;

    List<UUID> versionIds;
    List<UUID> dataIds;

//    @Override
//    public ProjectDTO readJson(String jsonString)
//    {
//        JsonObject json = new JsonObject(jsonString);
//
//        return ProjectDTO.builder()
//                .name(json.getString(ProjectEntity.PROJECT_NAME_KEY))
//                .annotationType(json.getString(ProjectEntity.ANNOTATION_TYPE_KEY))
//                .currentVersionId(json.getString(ProjectEntity.CURRENT_VERSION_KEY))
//                .build();
//    }
//
//    @Override
//    public String toJson()
//    {
//        return new JsonObject()
//                .put(ProjectEntity.PROJECT_NAME_KEY, name)
//                .put(ProjectEntity.ANNOTATION_TYPE_KEY, annotationType)
//                .put(ProjectEntity.CURRENT_VERSION_KEY, currentVersionId)
//                .encodePrettily();
//    }

}
