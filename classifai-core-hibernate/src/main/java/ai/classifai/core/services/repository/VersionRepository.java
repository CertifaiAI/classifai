package ai.classifai.core.services.repository;

import ai.classifai.core.entities.Version;
import ai.classifai.core.entities.dto.VersionDTO;

import java.util.UUID;

public interface VersionRepository extends Repository<Version, VersionDTO, UUID>
{
    Version updateModifiedAt(Version version);
}
