package ai.classifai.core.entities;

import ai.classifai.core.entities.dto.VersionDTO;
import ai.classifai.core.entities.traits.HasDTO;
import ai.classifai.core.entities.traits.HasId;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface Version extends HasId<UUID>, HasDTO<VersionDTO>
{
    Instant getCreatedAt();
    Instant getModifiedAt();

    List<Label> getLabelList();
}
