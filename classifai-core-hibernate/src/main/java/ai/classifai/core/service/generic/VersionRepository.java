package ai.classifai.core.service.generic;

import ai.classifai.core.entity.model.generic.Label;
import ai.classifai.core.entity.model.generic.Project;
import ai.classifai.core.entity.model.generic.Version;
import ai.classifai.core.entity.dto.generic.VersionDTO;

import java.util.List;
import java.util.UUID;

public interface VersionRepository extends Repository<Version, VersionDTO, UUID>
{
    Version updateModifiedAt(Version version);

    Version resetCreatedAt(Version version);

    List<Version> listByProject(Project project);
}
