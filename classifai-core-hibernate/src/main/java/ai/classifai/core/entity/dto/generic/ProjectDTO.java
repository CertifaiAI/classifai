package ai.classifai.core.entity.dto.generic;

import ai.classifai.util.project.ProjectInfra;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * DTO class representing Project entity
 *
 * @author YinChuangSum
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProjectDTO {
    @Builder.Default
    private UUID id = null;

    private String name;
    private Integer type;
    private String path;

    @Builder.Default
    private Boolean isNew = true;

    @Builder.Default
    private Boolean isStarred = false;

    @Builder.Default
    private Integer infra = ProjectInfra.ON_PREMISE.ordinal();

    @Builder.Default
    private UUID currentVersionId = null;

    @Builder.Default
    private List<UUID> versionIdList = new ArrayList<>();

    @Builder.Default
    private List<UUID> dataIdList = new ArrayList<>();
}
